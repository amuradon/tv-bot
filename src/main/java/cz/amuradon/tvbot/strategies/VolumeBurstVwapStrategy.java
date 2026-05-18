package cz.amuradon.tvbot.strategies;

import java.math.BigDecimal;
import java.util.LinkedList;

import com.binance.connector.client.common.websocket.service.StreamBlockingQueueWrapper;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.Side;
import com.binance.connector.client.derivatives_trading_usds_futures.websocket.stream.api.DerivativesTradingUsdsFuturesWebSocketStreams;
import com.binance.connector.client.derivatives_trading_usds_futures.websocket.stream.model.AggregateTradeStreamsRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.websocket.stream.model.AggregateTradeStreamsResponse;

import cz.amuradon.tvbot.BinanceRestClientFacade;
import cz.amuradon.tvbot.indicators.Kline;

public class VolumeBurstVwapStrategy extends AbstractStrategy {

	private static final int MINUTE_IN_MILLIS = 60000;

	private final DerivativesTradingUsdsFuturesWebSocketStreams wsStreams;
	
	private final int volumeSmaLength = 20;
	
	private final int timeframe = 5; // in minutes
	
	private final long timeframeInMillis;
	
	private final LinkedList<Kline> klines;
	
	private final int klineMinLength = 200; // based on indicators
	
	private Kline currentKline;
		
	public VolumeBurstVwapStrategy(BinanceRestClientFacade restClient,
			DerivativesTradingUsdsFuturesWebSocketStreams wsStreams) {
		super(restClient);
		this.wsStreams = wsStreams;
		klines = new LinkedList<>();
		
		timeframeInMillis = timeframe * MINUTE_IN_MILLIS;
		
		// To force to create first kline
		currentKline = new Kline(0l, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
	}

	@Override
	void startInternal(String symbol, BigDecimal quantity) {
		
		AggregateTradeStreamsRequest request = new AggregateTradeStreamsRequest();
		request.setSymbol(symbol);
		StreamBlockingQueueWrapper<AggregateTradeStreamsResponse> queue = wsStreams.aggregateTradeStreams(request);
		
		while(true) {
			AggregateTradeStreamsResponse response = null;
			try {
				response = queue.take();
			} catch (InterruptedException e) {
				throw new IllegalStateException(String.format("Strategy %s interrupted", getClass().getSimpleName()), e);
			}
			
			manageKlines(response);
			// TODO pocitat volume SMA
			
		
			restClient.newOrder(symbol, Side.BUY, quantity, false, this.getClass().getSimpleName());
		}
	}

	private void manageKlines(AggregateTradeStreamsResponse response) {
		
		Long tradeTime = response.getT();
		BigDecimal price = new BigDecimal(response.getpLowerCase());
		BigDecimal volume = new BigDecimal(response.getqLowerCase());
		
		if (tradeTime <= currentKline.closeTime) {
			currentKline.low = currentKline.low.min(price);
			currentKline.high = currentKline.high.max(price);
			currentKline.close = price;
			currentKline.volume = currentKline.volume.add(volume);
		} else {
			long startTime = tradeTime % timeframeInMillis;
			currentKline = new Kline(startTime, startTime + timeframeInMillis - 1, price, volume);
			klines.add(currentKline);
			
			if (klines.size() > klineMinLength) {
				klines.remove();
			}
		}
	}

}
