package cz.amuradon.tvbot;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


public record WebhookData(
		@JsonProperty(required = true) String symbol,
		@JsonProperty(required = true) String side,
		@JsonProperty(required = true) BigDecimal quantity,
		@JsonProperty(required = true) boolean reduceOnly,
		String newClientOrderId,
		@JsonProperty(required = true) BigDecimal stopLoss,
		@JsonProperty(required = true) String userUuid) {

}
