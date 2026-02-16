package com.algotrader.ingestor.client;

import com.algotrader.shared.model.Tick;
import com.fasterxml.jackson.databind.JsonNode;
import com.algotrader.shared.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for the Binance REST API.
 * Uses {@link java.net.http.HttpClient} (no external HTTP libs).
 */
public class BinanceClient {

    private static final Logger log = LoggerFactory.getLogger(BinanceClient.class);

    private static final String BASE_URL = "https://api.binance.com/api/v3";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final String baseUrl;

    public BinanceClient() {
        this(BASE_URL);
    }

    /**
     * Constructor allowing a custom base URL (useful for testing).
     */
    public BinanceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    /**
     * Fetches the current price for a given trading pair from Binance.
     *
     * @param symbol the trading pair (e.g. "BTCUSDT")
     * @return a {@link Tick} with the symbol, price, and current timestamp
     * @throws RuntimeException if the HTTP request fails or response cannot be
     *                          parsed
     */
    public Tick fetchTicker(String symbol) {
        String url = baseUrl + "/ticker/price?symbol=" + symbol;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Binance API returned HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            JsonNode json = JsonUtil.mapper().readTree(response.body());
            String sym = json.get("symbol").asText();
            BigDecimal price = new BigDecimal(json.get("price").asText());

            Tick tick = Tick.of(sym, price);
            log.info("Fetched ticker: {} @ {}", tick.symbol(), tick.price());
            return tick;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Binance request interrupted for " + symbol, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ticker for " + symbol, e);
        }
    }
}
