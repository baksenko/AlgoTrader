package algotrader.ingestor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import algotrader.ingestor.clients.BinanceClient;
import algotrader.shared.utils.JsonUtil;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        port(8080);

        get("/health", (req, res) -> "OK");

        System.out.println("Ingestor Service started on port 8080...");

        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        String redisUrl = "redis://" + redisHost + ":6379";

        RedisPublisher publisher = new RedisPublisher(redisUrl);
        BinanceClient binanceClient = new BinanceClient();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                var tick = binanceClient.getPrice("BTCUSDT");

                if (tick != null) {
                    String json = JsonUtil.toJson(tick);

                    publisher.publish("market_data", json);

                    System.out.println("Published: " + json);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
