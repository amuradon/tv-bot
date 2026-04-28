package cz.amuradon.tvbot;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderResponse;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WeebhookProcessor {

	public static final String CHANNEL_NAME = "webhookProcessing";
	
	private final OrderManager restClient;
	
	@Inject
	public WeebhookProcessor(OrderManager restClient) {
		this.restClient = restClient;
	}
	
	@Incoming(CHANNEL_NAME)
	public Uni<Void> handle(WebhookData data) {
		String errorReason = restClient.isSupported(data.symbol());
		if (errorReason != null) {
			Log.warnf("Symbol %s not supported: %s", data.symbol(), errorReason);
			return Uni.createFrom().voidItem();
		}
		
		// TODO validace, ze hodnoty davaji smysl, napr. stopLoss cena neni >10% od aktualni ceny
		// XXX nastartovat strategii
		ApiResponse<NewOrderResponse> response = restClient.newOrder(data);
		
		return Uni.createFrom().voidItem();
	}
}
