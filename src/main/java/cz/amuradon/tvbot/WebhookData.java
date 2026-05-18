package cz.amuradon.tvbot;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


public record WebhookData(
		@JsonProperty(required = true) String strategy,
		@JsonProperty(required = true) String symbol,
		@JsonProperty(required = true) BigDecimal quantity,
		@JsonProperty(required = true) String userUuid) {

}
