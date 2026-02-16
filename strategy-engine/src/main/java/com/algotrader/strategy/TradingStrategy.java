package com.algotrader.strategy;

import com.algotrader.shared.model.Signal;
import com.algotrader.shared.model.Tick;

import java.util.Optional;

/**
 * Core strategy contract.
 * <p>
 * Each implementation processes incoming {@link Tick}s and optionally emits
 * a {@link Signal} when its conditions are met.
 * </p>
 */
public interface TradingStrategy {

    /**
     * Processes a market tick and optionally produces a trading signal.
     *
     * @param tick the incoming market tick
     * @return a signal if the strategy's conditions are triggered, otherwise empty
     */
    Optional<Signal> process(Tick tick);

    /**
     * @return the human-readable name of this strategy
     */
    String name();
}
