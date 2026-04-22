package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BeanConfig {

	@ApplicationScoped
	@Produces
	public DerivativesTradingUsdsFuturesRestApi api(@ConfigProperty(name = "BINANCE_API_KEY") String apiKey,
			@ConfigProperty(name = "BINANCE_SECRET_KEY") String secretKey) {
		ClientConfiguration config = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setApiKey(apiKey);
		signatureConfiguration.setSecretKey(secretKey);
		config.setSignatureConfiguration(signatureConfiguration);
		return new DerivativesTradingUsdsFuturesRestApi(config);
	}
}
