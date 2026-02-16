package com.algotrader.shared.model

import spock.lang.Specification
import spock.lang.Subject

import java.math.BigDecimal
import java.time.Instant

class TickSpec extends Specification {

    def "should create a Tick with all fields"() {
        given: "a symbol, price, and timestamp"
        def symbol = "BTCUSDT"
        def price = new BigDecimal("42000.50")
        def timestamp = Instant.parse("2026-01-15T10:00:00Z")

        when: "a Tick is created"
        def tick = new Tick(symbol, price, timestamp)

        then: "all fields are correctly assigned"
        tick.symbol() == "BTCUSDT"
        tick.price() == new BigDecimal("42000.50")
        tick.timestamp() == timestamp
    }

    def "should create a Tick using the factory method with current timestamp"() {
        given: "a symbol and price"
        def before = Instant.now()

        when: "Tick.of() is called"
        def tick = Tick.of("ETHUSDT", new BigDecimal("2500.00"))

        then: "the tick has the correct symbol and price"
        tick.symbol() == "ETHUSDT"
        tick.price() == new BigDecimal("2500.00")

        and: "the timestamp is approximately now"
        !tick.timestamp().isBefore(before)
        !tick.timestamp().isAfter(Instant.now())
    }

    def "should support equality for identical Ticks"() {
        given: "two Ticks with the same data"
        def ts = Instant.parse("2026-02-01T12:00:00Z")
        def tick1 = new Tick("BTCUSDT", new BigDecimal("50000"), ts)
        def tick2 = new Tick("BTCUSDT", new BigDecimal("50000"), ts)

        expect: "they are equal (record semantics)"
        tick1 == tick2
        tick1.hashCode() == tick2.hashCode()
    }

    def "should serialize and deserialize Tick to/from JSON"() {
        given: "a Tick"
        def tick = Tick.of("BTCUSDT", new BigDecimal("42000.50"))

        when: "serialized to JSON and back"
        def json = com.algotrader.shared.util.JsonUtil.toJson(tick)
        def restored = com.algotrader.shared.util.JsonUtil.fromJson(json, Tick)

        then: "the restored Tick matches the original"
        restored.symbol() == tick.symbol()
        restored.price().compareTo(tick.price()) == 0
        restored.timestamp() == tick.timestamp()
    }
}
