package algotrader.shared.models;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {
    UUID id;
    String symbol;
    BigDecimal price;
    BigDecimal amount;
    String status;
}
