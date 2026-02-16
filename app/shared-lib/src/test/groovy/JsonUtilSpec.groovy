import spock.lang.Specification
import java.time.Instant
import algotrader.shared.utils.JsonUtil

class JsonUtilSpec extends Specification {

    // Тестовый рекорд (DTO)
    static record TestTick(String symbol, BigDecimal price, Instant timestamp) {}

    def "should correctly serialize and deserialize a Record with Instant"() {
        given: "a complex object with time"
        def now = Instant.parse("2023-10-05T12:00:00Z")
        def tick = new TestTick("BTCUSDT", new BigDecimal("50000.50"), now)

        when: "we convert it to JSON"
        def json = JsonUtil.toJson(tick)

        then: "it should be a string"
        println("DEBUG JSON: " + json) // Полезно, чтобы увидеть формат глазами
        json.contains("BTCUSDT")
        json.contains("2023-10-05T12:00:00Z") // Проверяем, что дата читаема

        when: "we convert it back to object"
        def result = JsonUtil.fromJson(json, TestTick.class)

        then: "it should be equal to original"
        result == tick
        result.timestamp() == now
    }

    def "should ignore unknown properties"() {
        given: "JSON with an extra field (simulating API change)"
        def json = """
                {
                    "symbol": "ETHUSDT",
                    "price": 3000,
                    "timestamp": "2023-10-05T10:00:00Z",
                    "weird_new_field": "I break things"
                }
            """

        when: "we try to parse it"
        def result = JsonUtil.fromJson(json, TestTick.class)

        then: "it does not throw exception and maps known fields"
        result.symbol() == "ETHUSDT"
    }
}