package com.algotrader.strategy.redis;

import com.algotrader.shared.model.Signal;
import com.algotrader.shared.model.Tick;
import com.algotrader.shared.util.JsonUtil;
import com.algotrader.strategy.TradingStrategy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Subscribes to a Redis channel for incoming {@link Tick} messages,
 * runs each tick through a {@link TradingStrategy}, and publishes
 * any resulting {@link Signal} to an output channel.
 */
public class RedisListener implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RedisListener.class);

    private final RedisClient redisClient;
    private final StatefulRedisPubSubConnection<String, String> subConnection;
    private final StatefulRedisConnection<String, String> pubConnection;
    private final RedisCommands<String, String> pubCommands;
    private final TradingStrategy strategy;
    private final String outputChannel;

    public RedisListener(String redisUri, TradingStrategy strategy,
            String inputChannel, String outputChannel) {
        this.redisClient = RedisClient.create(redisUri);
        this.strategy = strategy;
        this.outputChannel = outputChannel;

        // ── Publisher connection (for emitting signals) ─────────────
        this.pubConnection = redisClient.connect();
        this.pubCommands = pubConnection.sync();

        // ── Subscriber connection (for receiving ticks) ─────────────
        this.subConnection = redisClient.connectPubSub();

        subConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                onMessage(channel, message);
            }
        });

        subConnection.sync().subscribe(inputChannel);
        log.info("Subscribed to '{}' — signals will be published to '{}'", inputChannel, outputChannel);
    }

    /**
     * Test-friendly constructor: inject pre-built components.
     */
    RedisListener(RedisClient redisClient,
            StatefulRedisPubSubConnection<String, String> subConnection,
            StatefulRedisConnection<String, String> pubConnection,
            RedisCommands<String, String> pubCommands,
            TradingStrategy strategy,
            String outputChannel) {
        this.redisClient = redisClient;
        this.subConnection = subConnection;
        this.pubConnection = pubConnection;
        this.pubCommands = pubCommands;
        this.strategy = strategy;
        this.outputChannel = outputChannel;
    }

    private void onMessage(String channel, String message) {
        try {
            Tick tick = JsonUtil.fromJson(message, Tick.class);
            log.debug("Received tick on '{}': {} @ {}", channel, tick.symbol(), tick.price());

            Optional<Signal> signal = strategy.process(tick);

            signal.ifPresent(s -> {
                String json = JsonUtil.toJson(s);
                pubCommands.publish(outputChannel, json);
                log.info("Published {} signal to '{}': {}", s.type(), outputChannel, json);
            });

        } catch (Exception e) {
            log.error("Error processing message from '{}': {}", channel, e.getMessage(), e);
        }
    }

    /**
     * Pings Redis to verify connectivity.
     */
    public boolean isConnected() {
        try {
            return "PONG".equals(pubCommands.ping());
        } catch (Exception e) {
            log.warn("Redis ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        log.info("Closing Redis listener...");
        subConnection.close();
        pubConnection.close();
        redisClient.shutdown();
    }
}
