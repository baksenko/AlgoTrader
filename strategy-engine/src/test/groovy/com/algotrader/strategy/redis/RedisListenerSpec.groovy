package com.algotrader.strategy.redis

import com.algotrader.shared.model.Signal
import com.algotrader.shared.model.SignalType
import com.algotrader.shared.model.Tick
import com.algotrader.shared.util.JsonUtil
import com.algotrader.strategy.TradingStrategy
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import spock.lang.Specification

class RedisListenerSpec extends Specification {

    def "should publish a signal to output channel when strategy emits BUY"() {
        given: "mocked Redis components"
        def pubCommands = Mock(RedisCommands)
        def subConnection = Mock(StatefulRedisPubSubConnection)
        def pubConnection = Mock(StatefulRedisConnection) {
            sync() >> pubCommands
        }
        def redisClient = Mock(RedisClient)

        and: "a strategy that returns a BUY signal"
        def mockSignal = Signal.of(SignalType.BUY, "BTCUSDT", new BigDecimal("42000"), "TEST")
        def strategy = Mock(TradingStrategy) {
            process(_) >> Optional.of(mockSignal)
        }

        and: "a RedisListener using the test constructor"
        def listener = new RedisListener(
                redisClient, subConnection, pubConnection, pubCommands,
                strategy, "trading_signals"
        )

        when: "a tick message arrives (simulated via reflection on the private onMessage)"
        def tick = Tick.of("BTCUSDT", new BigDecimal("42000"))
        def tickJson = JsonUtil.toJson(tick)

        // Invoke the private onMessage method
        def onMessage = RedisListener.getDeclaredMethod("onMessage", String, String)
        onMessage.setAccessible(true)
        onMessage.invoke(listener, "market_data", tickJson)

        then: "the signal is published as JSON to the output channel"
        1 * pubCommands.publish("trading_signals", { String json ->
            def signal = JsonUtil.fromJson(json, Signal)
            signal.type() == SignalType.BUY && signal.symbol() == "BTCUSDT"
        })
    }

    def "should NOT publish when strategy returns empty"() {
        given: "a strategy that returns no signal"
        def pubCommands = Mock(RedisCommands)
        def subConnection = Mock(StatefulRedisPubSubConnection)
        def pubConnection = Mock(StatefulRedisConnection) {
            sync() >> pubCommands
        }
        def redisClient = Mock(RedisClient)

        def strategy = Mock(TradingStrategy) {
            process(_) >> Optional.empty()
        }

        def listener = new RedisListener(
                redisClient, subConnection, pubConnection, pubCommands,
                strategy, "trading_signals"
        )

        when: "a tick message arrives"
        def tick = Tick.of("BTCUSDT", new BigDecimal("42000"))
        def tickJson = JsonUtil.toJson(tick)

        def onMessage = RedisListener.getDeclaredMethod("onMessage", String, String)
        onMessage.setAccessible(true)
        onMessage.invoke(listener, "market_data", tickJson)

        then: "no signal is published"
        0 * pubCommands.publish(_, _)
    }
}
