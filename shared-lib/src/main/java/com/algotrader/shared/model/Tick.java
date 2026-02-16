package com.algotrader.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable market tick representing a single price snapshot.
 *
 * @param symbol    the trading pair symbol (e.g. "BTCUSDT")
 * @param price     the current price
 * @param timestamp the time the tick was captured
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Tick(
        String symbol,
        BigDecimal price,
        Instant timestamp) {

    /**
     * Factory: create a Tick stamped at the current instant.
     */
    public static Tick of(String symbol, BigDecimal price) {
        return new Tick(symbol, price, Instant.now());
    }
}
