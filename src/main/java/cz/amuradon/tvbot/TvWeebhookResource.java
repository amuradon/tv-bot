package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
	
	@Inject
	public TvWeebhookResource(@ConfigProperty(name = "BINANCE_API_KEY")	String apiKey,
			@ConfigProperty(name = "BINANCE_SECRET_KEY") String secretKey) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}
	
	
	@POST
	public void handle(WebhookData data) {
		Log.infof("Received webhook request %s", data);
		
		ClientConfiguration config = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setApiKey(apiKey);
		signatureConfiguration.setSecretKey(secretKey);
		config.setSignatureConfiguration(signatureConfiguration);
	}
}
