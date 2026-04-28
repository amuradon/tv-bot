package cz.amuradon.tvbot;

import com.fasterxml.jackson.annotation.JsonProperty;


public record WebhookData(
		@JsonProperty(required = true) String symbol,
		String newClientOrderId,
		@JsonProperty(required = true) String userUuid) {

}
