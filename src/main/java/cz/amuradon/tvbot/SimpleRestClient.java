package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewAlgoOrderRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewAlgoOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.Side;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class SimpleRestClient {

	private DerivativesTradingUsdsFuturesRestApi api;

	@Inject
	public SimpleRestClient(@ConfigProperty(name = "BINANCE_API_KEY") String apiKey,
			@ConfigProperty(name = "BINANCE_SECRET_KEY") String secretKey) {
		ClientConfiguration config = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setApiKey(apiKey);
		signatureConfiguration.setSecretKey(secretKey);
		config.setSignatureConfiguration(signatureConfiguration);
		api = new DerivativesTradingUsdsFuturesRestApi(config);
	}
	
	@Retry(maxRetries = 3)
	public ApiResponse<NewOrderResponse> newOrder(WebhookData data) {
		NewOrderRequest newOrderRequest = new NewOrderRequest()
				.symbol(normalizeSymbol(data.symbol()))
				.side("buy".equalsIgnoreCase(data.side()) ? Side.BUY : Side.SELL)
				.type("MARKET")
				.quantity(data.quantity())
				.reduceOnly(data.reduceOnly() ? "true" : "false")
				.newClientOrderId(data.newClientOrderId());
		return api.newOrder(newOrderRequest);
	}

	private String normalizeSymbol(String symbol) {
		return symbol.replace(".P", "");
	}
	
	@Retry(maxRetries = 5, delay = 500)
	public ApiResponse<NewAlgoOrderResponse> stopLoss(WebhookData data) {
		NewAlgoOrderRequest stopLossRequest = new NewAlgoOrderRequest()
				.symbol(normalizeSymbol(data.symbol()))
				.side(Side.SELL)
				.type("STOP_MARKET")
				.triggerPrice(data.stopLoss())
				.algoType("CONDITIONAL")
				.closePosition("true");;
		return api.newAlgoOrder(stopLossRequest);
	}
}
