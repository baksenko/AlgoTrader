package com.algotrader.strategy;

import com.algotrader.strategy.impl.SimpleMovingAverageStrategy;
import com.algotrader.strategy.redis.RedisListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

/**
 * Entry point for the Strategy Engine service.
 *
 * <ul>
 * <li>Subscribes to Redis channel {@code market_data} for incoming ticks.</li>
 * <li>Applies the SMA crossover strategy.</li>
 * <li>Publishes resulting signals to Redis channel
 * {@code trading_signals}.</li>
 * <li>Exposes a SparkJava health endpoint on port 8081.</li>
 * </ul>
 */
public class StrategyEngineApp {

    private static final Logger log = LoggerFactory.getLogger(StrategyEngineApp.class);

    private static final String INPUT_CHANNEL = "market_data";
    private static final String OUTPUT_CHANNEL = "trading_signals";
    private static final int SMA_WINDOW = 5;
    private static final int HTTP_PORT = 8081;

    public static void main(String[] args) {
        String redisUri = System.getenv().getOrDefault("REDIS_URI", "redis://localhost:6379");
        int smaWindow = Integer.parseInt(
                System.getenv().getOrDefault("SMA_WINDOW", String.valueOf(SMA_WINDOW)));

        // ── Strategy ────────────────────────────────────────────────────
        var strategy = new SimpleMovingAverageStrategy(smaWindow);
        log.info("Initialized strategy: {} (window={})", strategy.name(), smaWindow);

        // ── Redis Listener (subscribe + publish) ────────────────────────
        var listener = new RedisListener(redisUri, strategy, INPUT_CHANNEL, OUTPUT_CHANNEL);

        // ── SparkJava HTTP server ───────────────────────────────────────
        port(HTTP_PORT);

        get("/health", (req, res) -> {
            res.type("application/json");
            boolean redisOk = listener.isConnected();
            int status = redisOk ? 200 : 503;
            res.status(status);
            return "{\"status\":\"" + (redisOk ? "UP" : "DOWN")
                    + "\",\"service\":\"strategy-engine\""
                    + ",\"strategy\":\"" + strategy.name() + "\""
                    + ",\"redis\":" + redisOk + "}";
        });

        log.info("Strategy Engine HTTP server started on port {}", HTTP_PORT);

        // ── Graceful shutdown ───────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Strategy Engine...");
            listener.close();
            stop();
            log.info("Strategy Engine stopped.");
        }));
    }
}
