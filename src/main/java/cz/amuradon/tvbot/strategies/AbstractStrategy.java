package cz.amuradon.tvbot.strategies;

import java.math.BigDecimal;

import cz.amuradon.tvbot.BinanceRestClientFacade;
import io.quarkus.logging.Log;

public abstract class AbstractStrategy implements Strategy {

	final BinanceRestClientFacade restClient;
	
	public AbstractStrategy(BinanceRestClientFacade restClient) {
		this.restClient = restClient;
	}
	
	@Override
	public void start(String symbol, BigDecimal quantity) {
		String errorReason = restClient.isSupported(symbol);
		if (errorReason != null) {
			Log.warnf("Symbol %s not supported: %s", symbol, errorReason);
		}
		
		startInternal(symbol, quantity);
	}
	
	abstract void startInternal(String symbol, BigDecimal quantity);
}
