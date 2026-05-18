package cz.amuradon.tvbot;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import cz.amuradon.tvbot.strategies.Strategy;
import cz.amuradon.tvbot.strategies.StrategyFactory;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebhookProcessor {

	public static final String CHANNEL_NAME = "webhookProcessing";
	
	private final StrategyFactory strategyFactory;
	
	@Inject
	public WebhookProcessor(StrategyFactory strategyFactory) {
		this.strategyFactory = strategyFactory;
	}
	
	@Incoming(CHANNEL_NAME)
	public Uni<Void> handle(WebhookData data) {
		Strategy strategy = strategyFactory.create(data.strategy());
		strategy.start(data.symbol(), data.quantity());
		
		return Uni.createFrom().voidItem();
	}
}
