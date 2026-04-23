package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewAlgoOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderResponse;
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

	private final OrderManager restClient;
	
	private final String userUuid;
	
	private final ObjectMapper mapper;
	
	@Inject
	public TvWeebhookResource(OrderManager restClient,
			@ConfigProperty(name = "TVBOT_USER_UUID") String userUuid,
			ObjectMapper mapper) {
		this.restClient = restClient;
		this.userUuid = userUuid;
		this.mapper = mapper;
	}
	
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
	@POST
	public RestResponse<Object> handle(String body) {
		Log.debugf("Received body %s", body);
		
		WebhookData data;
		try {
			// Due TradingView sending Content-Type: text/plain MIME type, I need to parse it myself
			data = mapper.readValue(body, WebhookData.class);
		} catch (JsonProcessingException e) {
			Log.errorf(e, "Not able to parse body as JSON: %s", body);
			return ResponseBuilder.create(Status.BAD_REQUEST).entity("Not able to parse body as JSON").build();
		}
		Log.infof("Received webhook request for %s, %s, %s", data.symbol(), data.side(), data.newClientOrderId());
		
		if (data.userUuid() == null || !data.userUuid().equals(userUuid)) {
			Log.errorf("Request for %s, %s, %s unauthorized", data.symbol(), data.side(), data.newClientOrderId());
			return ResponseBuilder.create(Status.UNAUTHORIZED).build();
		}
		
		String errorReason = restClient.isSupported(data.symbol());
		if (errorReason != null) {
			return ResponseBuilder.create(Status.BAD_REQUEST)
					.entity(String.format("Not supported symbol %s, error: %s", data.symbol(), errorReason)).build();
		}
		
		// TODO validace, ze hodnoty davaji smysl, napr. stopLoss cena neni >10% od aktualni ceny
		ApiResponse<NewOrderResponse> response = restClient.newOrder(data);
		
		Log.debugf("New order response %d %s", response.getStatusCode(), response.getData());
		
		if (response.getStatusCode() < 200 && response.getStatusCode() >= 300) {
			return ResponseBuilder.create(response.getStatusCode()).entity(response.getData()).build();
		}
		
		if ("buy".equalsIgnoreCase(data.side())) {
			ApiResponse<NewAlgoOrderResponse> stopLossResponse = restClient.stopLoss(data);
			if (stopLossResponse.getStatusCode() < 200 && stopLossResponse.getStatusCode() >= 300) {
				return ResponseBuilder.create(stopLossResponse.getStatusCode()).entity(stopLossResponse.getData()).build();
			}
		}
		
		return ResponseBuilder.ok().build();
	}
}
