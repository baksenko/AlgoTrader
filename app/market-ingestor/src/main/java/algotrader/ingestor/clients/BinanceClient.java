package algotrader.ingestor.clients;

import algotrader.shared.models.Ticker;
import algotrader.shared.utils.JsonUtil;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;

public class BinanceClient {

    private final HttpClient httpClient;

    public BinanceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    public Ticker getPrice(String symbol) {
        try {
            String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            record BinanceResponse(String symbol, BigDecimal price) {}

            BinanceResponse binanceData = JsonUtil.fromJson(response.body(), BinanceResponse.class);

            return new Ticker(binanceData.symbol(), binanceData.price(), Timestamp.from(Instant.now()));

        } catch (Exception e) {
            System.err.println("Error fetching price: " + e.getMessage());
            return null;
        }
    }
}
