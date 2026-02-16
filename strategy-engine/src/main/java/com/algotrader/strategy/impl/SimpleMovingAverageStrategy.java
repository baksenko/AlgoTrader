package com.algotrader.strategy.impl;

import com.algotrader.shared.model.Signal;
import com.algotrader.shared.model.SignalType;
import com.algotrader.shared.model.Tick;
import com.algotrader.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Simple Moving Average (SMA) crossover strategy.
 * <p>
 * Emits a {@link SignalType#BUY} when the current price crosses <b>above</b>
 * the SMA,
 * and a {@link SignalType#SELL} when it crosses <b>below</b>.
 * <p>
 * State is maintained in a sliding window of the last {@code N} prices.
 */
public class SimpleMovingAverageStrategy implements TradingStrategy {

    private static final Logger log = LoggerFactory.getLogger(SimpleMovingAverageStrategy.class);

    private final int window;
    private final LinkedList<BigDecimal> priceWindow = new LinkedList<>();

    /**
     * Tracks whether the previous tick's price was above the SMA.
     * {@code null} means we haven't established a baseline yet.
     */
    private Boolean wasAboveSma = null;

    /**
     * @param window the number of prices to use for the SMA calculation (must be ≥
     *               1)
     */
    public SimpleMovingAverageStrategy(int window) {
        if (window <= 0) {
            throw new IllegalArgumentException("SMA window must be > 0, got: " + window);
        }
        this.window = window;
    }

    @Override
    public Optional<Signal> process(Tick tick) {
        priceWindow.addLast(tick.price());

        // Evict oldest price if window is full
        if (priceWindow.size() > window) {
            priceWindow.removeFirst();
        }

        // Not enough data yet — cannot compute SMA
        if (priceWindow.size() < window) {
            return Optional.empty();
        }

        BigDecimal sma = calculateSma();
        boolean isAboveSma = tick.price().compareTo(sma) > 0;

        log.debug("{} price={} SMA-{}={} above={}",
                tick.symbol(), tick.price(), window, sma, isAboveSma);

        Optional<Signal> signal = Optional.empty();

        if (wasAboveSma != null) {
            if (isAboveSma && !wasAboveSma) {
                // Price crossed ABOVE the SMA → BUY
                signal = Optional.of(Signal.of(SignalType.BUY, tick.symbol(), tick.price(), name()));
                log.info("BUY signal: {} @ {} (SMA-{}={})", tick.symbol(), tick.price(), window, sma);
            } else if (!isAboveSma && wasAboveSma) {
                // Price crossed BELOW the SMA → SELL
                signal = Optional.of(Signal.of(SignalType.SELL, tick.symbol(), tick.price(), name()));
                log.info("SELL signal: {} @ {} (SMA-{}={})", tick.symbol(), tick.price(), window, sma);
            }
        }

        wasAboveSma = isAboveSma;
        return signal;
    }

    @Override
    public String name() {
        return "SMA-" + window;
    }

    private BigDecimal calculateSma() {
        BigDecimal sum = priceWindow.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(window), 10, RoundingMode.HALF_UP);
    }
}
