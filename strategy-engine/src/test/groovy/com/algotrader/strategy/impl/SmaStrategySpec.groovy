package com.algotrader.strategy.impl

import com.algotrader.shared.model.Signal
import com.algotrader.shared.model.SignalType
import com.algotrader.shared.model.Tick
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.math.BigDecimal
import java.time.Instant

class SmaStrategySpec extends Specification {

    @Subject
    SimpleMovingAverageStrategy strategy

    def setup() {
        // SMA with window of 3 prices
        strategy = new SimpleMovingAverageStrategy(3)
    }

    def "should return HOLD when not enough data to compute SMA"() {
        given: "fewer ticks than the SMA window"
        def tick1 = Tick.of("BTCUSDT", new BigDecimal("100"))
        def tick2 = Tick.of("BTCUSDT", new BigDecimal("110"))

        expect: "no signal is emitted (empty Optional)"
        strategy.process(tick1) == Optional.empty()
        strategy.process(tick2) == Optional.empty()
    }

    def "should emit BUY when price crosses SMA from below"() {
        given: "a strategy with window=3 and a price sequence where the latest price crosses above SMA"
        strategy = new SimpleMovingAverageStrategy(3)

        and: "first 3 ticks establish a declining SMA baseline"
        // Prices: 100, 90, 80 → SMA = 90.0, last price (80) is below SMA → was below
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("100")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("90")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("80")))
        // SMA after 3rd tick = (100+90+80)/3 = 90.0, price=80 < 90 → below

        when: "price jumps above the SMA"
        // Prices window becomes: [90, 80, 120] → SMA = 96.67, price=120 > 96.67 → crosses above
        def result = strategy.process(Tick.of("BTCUSDT", new BigDecimal("120")))

        then: "a BUY signal is emitted"
        result.isPresent()
        result.get().type() == SignalType.BUY
        result.get().symbol() == "BTCUSDT"
        result.get().price() == new BigDecimal("120")
        result.get().strategy() == "SMA-3"
    }

    def "should emit SELL when price crosses SMA from above"() {
        given: "a strategy with window=3 and a price sequence where price crosses below SMA"
        strategy = new SimpleMovingAverageStrategy(3)

        and: "first 3 ticks establish a rising baseline"
        // Prices: 80, 90, 100 → SMA = 90.0, price=100 > 90 → above
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("80")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("90")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("100")))

        when: "price drops below the SMA"
        // Window: [90, 100, 70] → SMA = 86.67, price=70 < 86.67 → crosses below
        def result = strategy.process(Tick.of("BTCUSDT", new BigDecimal("70")))

        then: "a SELL signal is emitted"
        result.isPresent()
        result.get().type() == SignalType.SELL
        result.get().symbol() == "BTCUSDT"
        result.get().price() == new BigDecimal("70")
    }

    def "should return empty when price stays on same side of SMA"() {
        given: "a strategy with window=3"
        strategy = new SimpleMovingAverageStrategy(3)

        and: "prices consistently above SMA"
        // Prices: 100, 110, 120 → SMA = 110, price=120 > 110 → above
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("100")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("110")))
        strategy.process(Tick.of("BTCUSDT", new BigDecimal("120")))

        when: "price continues above SMA (no crossover)"
        // Window: [110, 120, 130] → SMA = 120, price=130 > 120 → still above
        def result = strategy.process(Tick.of("BTCUSDT", new BigDecimal("130")))

        then: "no signal is emitted"
        result == Optional.empty()
    }

    @Unroll
    def "SMA crossover detection with prices #prices should produce #expectedSignal"() {
        given: "a fresh SMA-3 strategy"
        def sma = new SimpleMovingAverageStrategy(3)

        when: "all prices are processed in sequence"
        def results = prices.collect { p ->
            sma.process(Tick.of("BTCUSDT", new BigDecimal(p.toString())))
        }

        then: "the last result matches the expected signal"
        def lastResult = results.last()
        if (expectedSignal == null) {
            !lastResult.isPresent()
        } else {
            lastResult.isPresent()
            lastResult.get().type() == expectedSignal
        }

        where: "various price sequences"
        prices                      || expectedSignal
        [100, 90, 80, 120]          || SignalType.BUY    // crosses above SMA
        [80, 90, 100, 70]           || SignalType.SELL   // crosses below SMA
        [100, 110, 120, 130]        || null              // stays above, no crossover
        [100, 90, 80, 70]           || null              // stays below, no crossover
        [100, 100, 100, 100]        || null              // flat, no crossover
    }

    def "should have correct strategy name"() {
        expect:
        strategy.name() == "SMA-3"
    }

    def "should reject invalid window size"() {
        when:
        new SimpleMovingAverageStrategy(0)

        then:
        thrown(IllegalArgumentException)
    }
}
