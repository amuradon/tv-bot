package cz.amuradon.tvbot;

public record WebhookData(String symbol,
		String side,
		double quantity,
		boolean reduceOnly,
		String newClientOrderId,
		String userUuid) {

}
