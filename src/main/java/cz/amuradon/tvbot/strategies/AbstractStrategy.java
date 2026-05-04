package cz.amuradon.tvbot.strategies;

import cz.amuradon.tvbot.BinanceRestClientFacade;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

public abstract class AbstractStrategy implements Strategy {

	private final BinanceRestClientFacade restClient;
	
	public AbstractStrategy(BinanceRestClientFacade restClient) {
		this.restClient = restClient;
	}
	
	@Override
	public void start(String symbol) {
		String errorReason = restClient.isSupported(symbol);
		if (errorReason != null) {
			Log.warnf("Symbol %s not supported: %s", symbol, errorReason);
		}
		
		startInternal(symbol);
	}
	
	abstract void startInternal(String symbol);
}
