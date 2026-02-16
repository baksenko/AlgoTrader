package com.algotrader.ingestor.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes messages to Redis channels using the Lettuce client.
 * Wraps a persistent {@link StatefulRedisConnection} for efficient pub/sub.
 */
public class RedisPublisher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RedisPublisher.class);

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;

    public RedisPublisher(String redisUri) {
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.commands = connection.sync();
        log.info("Connected to Redis at {}", redisUri);
    }

    /**
     * Test-friendly constructor: inject pre-built Lettuce components.
     */
    RedisPublisher(RedisClient redisClient,
            StatefulRedisConnection<String, String> connection,
            RedisCommands<String, String> commands) {
        this.redisClient = redisClient;
        this.connection = connection;
        this.commands = commands;
    }

    /**
     * Publishes a message to the specified Redis channel.
     *
     * @param channel the channel name (e.g. "market_data")
     * @param message the serialized message payload (JSON)
     * @return the number of subscribers that received the message
     */
    public long publish(String channel, String message) {
        long receivers = commands.publish(channel, message);
        log.debug("Published to channel '{}' — {} receiver(s)", channel, receivers);
        return receivers;
    }

    /**
     * Pings Redis to verify connectivity.
     *
     * @return true if Redis responds with "PONG"
     */
    public boolean isConnected() {
        try {
            return "PONG".equals(commands.ping());
        } catch (Exception e) {
            log.warn("Redis ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        log.info("Closing Redis connection...");
        connection.close();
        redisClient.shutdown();
    }
}
