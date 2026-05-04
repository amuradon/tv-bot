package cz.amuradon.tvbot.strategies;

import cz.amuradon.tvbot.BinanceRestClientFacade;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StrategyFactory {

	private final BinanceRestClientFacade restClient;
	
	public StrategyFactory(BinanceRestClientFacade restClient) {
		this.restClient = restClient;
	}

	public Strategy create(String name) {
		return switch (name) {
		case "volumeBurst" -> new VolumeBurstVwapStrategy(restClient);

		default -> throw new IllegalArgumentException("Unexpected value: " + name);
		};
	}
}
