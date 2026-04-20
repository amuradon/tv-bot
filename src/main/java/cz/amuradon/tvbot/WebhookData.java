package cz.amuradon.tvbot;

public record WebhookData(String symbol,
		String side,
		double quantity,
		boolean reduceOnly,
		String newClientOrderId,
		double stopLoss,
		String userUuid) {

}
