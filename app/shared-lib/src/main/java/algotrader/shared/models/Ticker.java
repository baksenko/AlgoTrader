package algotrader.shared.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Ticker {
    String symbol;
    BigDecimal price;
    Timestamp timestamp;

    public Ticker(String symbol_, BigDecimal price_,  Timestamp now) {
        symbol = symbol_;
        price = price_;
        timestamp = now;
    }
}
