package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/tvwebhook")
public class TvWeebhookResource {

	private final String apiKey;

	private final String secretKey;
	
	private final String userUuid;
	
	@Inject
	public TvWeebhookResource(@ConfigProperty(name = "BINANCE_API_KEY")	String apiKey,
			@ConfigProperty(name = "BINANCE_SECRET_KEY") String secretKey,
			@ConfigProperty(name = "TVBOT_USER_UUID") String userUuid) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.userUuid = userUuid;
	}
	
	
	@POST
	public RestResponse<Object> handle(WebhookData data) {
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
		
		return ResponseBuilder.ok().build();
	}
}
