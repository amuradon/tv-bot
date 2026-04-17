package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.Side;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/tvwebhook")
public class TvWeebhookResource {

	private final String apiKey;

	private final String secretKey;
	
	private final String userUuid;
	
	private final ObjectMapper mapper;
	
	@Inject
	public TvWeebhookResource(@ConfigProperty(name = "BINANCE_API_KEY")	String apiKey,
			@ConfigProperty(name = "BINANCE_SECRET_KEY") String secretKey,
			@ConfigProperty(name = "TVBOT_USER_UUID") String userUuid,
			ObjectMapper mapper) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.userUuid = userUuid;
		this.mapper = mapper;
	}
	
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
	@POST
	public RestResponse<Object> handle(String body) {
		Log.debugf("Received body %s", body);
		
		WebhookData data;
		try {
			data = mapper.readValue(body, WebhookData.class);
		} catch (JsonProcessingException e) {
			Log.error("Not able to parse body as JSON", e);
			return ResponseBuilder.create(Status.BAD_REQUEST).entity("Not able to parse body as JSON").build();
		}
		Log.infof("Received webhook request for %s, %s, %s", data.symbol(), data.side(), data.newClientOrderId());
		
		if (data.userUuid() == null || !data.userUuid().equals(userUuid)) {
			Log.errorf("Request for %s, %s, %s unauthorized", data.symbol(), data.side(), data.newClientOrderId());
			return ResponseBuilder.create(Status.UNAUTHORIZED).build();
		}
		
		ClientConfiguration config = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setApiKey(apiKey);
		signatureConfiguration.setSecretKey(secretKey);
		config.setSignatureConfiguration(signatureConfiguration);
		DerivativesTradingUsdsFuturesRestApi api = new DerivativesTradingUsdsFuturesRestApi(config);
		
		NewOrderRequest newOrderRequest = new NewOrderRequest()
				.symbol(data.symbol().replace(".P", "")) // Symbol will probably come from TV like BTCUSDT.P
				.side("buy".equalsIgnoreCase(data.side()) ? Side.BUY : Side.SELL)
				.type("MARKET")
				.quantity(data.quantity())
				.reduceOnly(data.reduceOnly() ? "true" : "false")
				.newClientOrderId(data.newClientOrderId());
		ApiResponse<NewOrderResponse> response = api.newOrder(newOrderRequest);
		
		Log.debugf("New order response %d %s", response.getStatusCode(), response.getData());
		
		if (response.getStatusCode() < 200 && response.getStatusCode() >= 300) {
			return ResponseBuilder.create(response.getStatusCode()).entity(response.getData()).build();
		}
		
		return ResponseBuilder.ok().build();
	}
}
