package cz.amuradon.tvbot.strategies;

import com.binance.connector.client.derivatives_trading_usds_futures.websocket.stream.api.DerivativesTradingUsdsFuturesWebSocketStreams;

import cz.amuradon.tvbot.BinanceRestClientFacade;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StrategyFactory {

	private final BinanceRestClientFacade restClient;
	
	private final DerivativesTradingUsdsFuturesWebSocketStreams wsStreams;
	
	public StrategyFactory(BinanceRestClientFacade restClient, DerivativesTradingUsdsFuturesWebSocketStreams wsStreams) {
		this.restClient = restClient;
		this.wsStreams = wsStreams;
	}

	public Strategy create(String name) {
		return switch (name) {
		case "volumeBurst" -> new VolumeBurstVwapStrategy(restClient, wsStreams);

		default -> throw new IllegalArgumentException("Unexpected value: " + name);
		};
	}
}
