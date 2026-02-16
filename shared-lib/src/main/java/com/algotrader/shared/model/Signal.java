package com.algotrader.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable trading signal emitted by a strategy.
 *
 * @param type      the signal direction (BUY / SELL / HOLD)
 * @param symbol    the trading pair (e.g. "BTCUSDT")
 * @param price     the price at which the signal was generated
 * @param strategy  the name of the strategy that produced this signal
 * @param timestamp the time the signal was generated
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Signal(
        SignalType type,
        String symbol,
        BigDecimal price,
        String strategy,
        Instant timestamp) {

    /**
     * Factory: create a Signal stamped at the current instant.
     */
    public static Signal of(SignalType type, String symbol, BigDecimal price, String strategy) {
        return new Signal(type, symbol, price, strategy, Instant.now());
    }
}
