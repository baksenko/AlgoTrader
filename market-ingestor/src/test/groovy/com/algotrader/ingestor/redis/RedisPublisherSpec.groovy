package com.algotrader.ingestor.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import spock.lang.Specification

class RedisPublisherSpec extends Specification {

    def "should publish a message and return receiver count"() {
        given: "a mocked Redis infrastructure"
        def commands = Mock(RedisCommands)
        def connection = Mock(StatefulRedisConnection) {
            sync() >> commands
        }
        def client = Mock(RedisClient) {
            connect() >> connection
        }

        and: "a RedisPublisher with injected mocks"
        def publisher = new RedisPublisher(client, connection, commands)

        when: "a message is published"
        def result = publisher.publish("market_data", '{"symbol":"BTCUSDT","price":"42000"}')

        then: "the commands.publish method is called"
        1 * commands.publish("market_data", '{"symbol":"BTCUSDT","price":"42000"}') >> 2L

        and: "the receiver count is returned"
        result == 2L
    }

    def "isConnected should return true when Redis responds with PONG"() {
        given:
        def commands = Mock(RedisCommands) {
            ping() >> "PONG"
        }
        def connection = Mock(StatefulRedisConnection) {
            sync() >> commands
        }
        def client = Mock(RedisClient) {
            connect() >> connection
        }
        def publisher = new RedisPublisher(client, connection, commands)

        expect:
        publisher.isConnected()
    }

    def "isConnected should return false when Redis is unreachable"() {
        given:
        def commands = Mock(RedisCommands) {
            ping() >> { throw new RuntimeException("Connection refused") }
        }
        def connection = Mock(StatefulRedisConnection) {
            sync() >> commands
        }
        def client = Mock(RedisClient) {
            connect() >> connection
        }
        def publisher = new RedisPublisher(client, connection, commands)

        expect:
        !publisher.isConnected()
    }
}
