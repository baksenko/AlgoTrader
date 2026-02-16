package com.algotrader.ingestor.client

import com.algotrader.shared.model.Tick
import com.sun.net.httpserver.HttpServer
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.AutoCleanup

class BinanceClientSpec extends Specification {

    @Shared
    HttpServer mockServer

    @Shared
    int port

    def setupSpec() {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0)
        port = mockServer.address.port

        mockServer.createContext("/api/v3/ticker/price") { exchange ->
            def query = exchange.requestURI.query ?: ""
            def symbol = query.replace("symbol=", "")

            def responseBody
            def statusCode

            if (symbol == "BTCUSDT") {
                responseBody = '{"symbol":"BTCUSDT","price":"42000.50"}'
                statusCode = 200
            } else if (symbol == "INVALID") {
                responseBody = '{"code":-1121,"msg":"Invalid symbol."}'
                statusCode = 400
            } else {
                responseBody = '{"symbol":"' + symbol + '","price":"100.00"}'
                statusCode = 200
            }

            exchange.sendResponseHeaders(statusCode, responseBody.bytes.length)
            exchange.responseBody.write(responseBody.bytes)
            exchange.responseBody.close()
        }

        mockServer.start()
    }

    def cleanupSpec() {
        mockServer?.stop(0)
    }

    def "should fetch BTCUSDT ticker and return a valid Tick"() {
        given: "a BinanceClient pointing at the mock server"
        def client = new BinanceClient("http://localhost:${port}/api/v3")

        when: "fetching the BTCUSDT ticker"
        Tick tick = client.fetchTicker("BTCUSDT")

        then: "the Tick has correct symbol and price"
        tick.symbol() == "BTCUSDT"
        tick.price() == new BigDecimal("42000.50")
        tick.timestamp() != null
    }

    def "should throw RuntimeException for non-200 responses"() {
        given: "a BinanceClient pointing at the mock server"
        def client = new BinanceClient("http://localhost:${port}/api/v3")

        when: "fetching an invalid symbol"
        client.fetchTicker("INVALID")

        then: "a RuntimeException is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("400")
    }

    def "should fetch any valid symbol"() {
        given:
        def client = new BinanceClient("http://localhost:${port}/api/v3")

        when:
        Tick tick = client.fetchTicker("ETHUSDT")

        then:
        tick.symbol() == "ETHUSDT"
        tick.price() == new BigDecimal("100.00")
    }
}
