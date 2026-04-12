package cz.amuradon.tvbot;

import java.math.BigDecimal;

public record WebhookData(String symbol, String side, BigDecimal quantity, boolean reduceOnly, String newClientOrderId) {

}
