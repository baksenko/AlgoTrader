package com.algotrader.ingestor;

import com.algotrader.ingestor.client.BinanceClient;
import com.algotrader.ingestor.redis.RedisPublisher;
import com.algotrader.shared.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

/**
 * Entry point for the Market Ingestor service.
 *
 * <ul>
 * <li>Exposes a SparkJava health endpoint on port 8080.</li>
 * <li>Polls Binance every 5 seconds for BTCUSDT price.</li>
 * <li>Publishes each {@link com.algotrader.shared.model.Tick} as JSON to Redis
 * channel {@code market_data}.</li>
 * </ul>
 */
public class MarketIngestorApp {

    private static final Logger log = LoggerFactory.getLogger(MarketIngestorApp.class);

    private static final String REDIS_CHANNEL = "market_data";
    private static final String DEFAULT_SYMBOL = "BTCUSDT";
    private static final int POLL_INTERVAL_SECONDS = 5;
    private static final int HTTP_PORT = 8080;

    public static void main(String[] args) {
        String redisUri = System.getenv().getOrDefault("REDIS_URI", "redis://localhost:6379");
        String symbol = System.getenv().getOrDefault("TRADE_SYMBOL", DEFAULT_SYMBOL);

        // ── Redis publisher ─────────────────────────────────────────────
        RedisPublisher publisher = new RedisPublisher(redisUri);

        // ── Binance HTTP client ─────────────────────────────────────────
        BinanceClient binanceClient = new BinanceClient();

        // ── SparkJava HTTP server ───────────────────────────────────────
        port(HTTP_PORT);

        get("/health", (req, res) -> {
            res.type("application/json");
            boolean redisOk = publisher.isConnected();
            int status = redisOk ? 200 : 503;
            res.status(status);
            return "{\"status\":\"" + (redisOk ? "UP" : "DOWN")
                    + "\",\"service\":\"market-ingestor\""
                    + ",\"redis\":" + redisOk + "}";
        });

        log.info("Market Ingestor HTTP server started on port {}", HTTP_PORT);

        // ── Scheduled price polling ─────────────────────────────────────
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "binance-poller");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                var tick = binanceClient.fetchTicker(symbol);
                String json = JsonUtil.toJson(tick);
                publisher.publish(REDIS_CHANNEL, json);
                log.info("Published tick to '{}': {}", REDIS_CHANNEL, json);
            } catch (Exception e) {
                log.error("Error during tick fetch/publish cycle", e);
            }
        }, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // ── Graceful shutdown ───────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Market Ingestor...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            publisher.close();
            stop();
            log.info("Market Ingestor stopped.");
        }));
    }
}
