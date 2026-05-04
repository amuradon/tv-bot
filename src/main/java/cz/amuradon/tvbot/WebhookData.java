package cz.amuradon.tvbot;

import com.fasterxml.jackson.annotation.JsonProperty;


public record WebhookData(
		@JsonProperty(required = true) String strategy,
		@JsonProperty(required = true) String symbol,
		@JsonProperty(required = true) String userUuid) {

}
