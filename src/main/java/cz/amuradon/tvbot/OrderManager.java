package cz.amuradon.tvbot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponseSymbolsInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponseSymbolsInnerFiltersInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewAlgoOrderRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewAlgoOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderRequest;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.NewOrderResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.Side;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderManager {

	private final DerivativesTradingUsdsFuturesRestApi api;
	
	private final Map<String, SymbolData> symbolData;

	@Inject
	public OrderManager(DerivativesTradingUsdsFuturesRestApi api) {
		this.api = api;
		symbolData = new HashMap<>();
	}
	
	@PostConstruct
	public void init() {
		ApiResponse<ExchangeInformationResponse> response = api.exchangeInformation();
		for (ExchangeInformationResponseSymbolsInner symbol : response.getData().getSymbols()) {
			if (symbol.getContractType().equalsIgnoreCase("PERPETUAL")
					|| symbol.getContractType().equalsIgnoreCase("TRADIFI_PERPETUAL")) {
				final SymbolData item = new SymbolData();
				item.pricePrecision = symbol.getPricePrecision().intValue();
				item.quantityPrecision = symbol.getQuantityPrecision().intValue();
				for (ExchangeInformationResponseSymbolsInnerFiltersInner filter : symbol.getFilters()) {
					if (filter.getFilterType().equalsIgnoreCase("MARKET_LOT_SIZE")) {
						item.stepSize = new BigDecimal(filter.getStepSize());
					} else if (filter.getFilterType().equalsIgnoreCase("PRICE_FILTER")) {
						item.tickSize = new BigDecimal(filter.getTickSize());
					}  
				}
			}
		}
	}
	
	public ApiResponse<NewOrderResponse> newOrder(WebhookData data) {
		final String symbol = normalizeSymbol(data.symbol());
		NewOrderRequest newOrderRequest = new NewOrderRequest()
				.symbol(symbol)
				.side("buy".equalsIgnoreCase(data.side()) ? Side.BUY : Side.SELL)
				.type("MARKET")
//				.quantity(normalizeQuantity(data.quantity(), symbol))
				.quantity(data.quantity().doubleValue())
				.reduceOnly(data.reduceOnly() ? "true" : "false")
				.newClientOrderId(data.newClientOrderId());
		
		// TODO ExchangeInfo muze byt stare a dojde k chybe -> podle typu chyby obnovit a zkusit znovu
		return api.newOrder(newOrderRequest);
	}

	private Double normalizeQuantity(BigDecimal quantity, String symbol) {
		SymbolData data = symbolData.get(symbol);
		return quantity.divide(data.stepSize, 0, RoundingMode.DOWN).multiply(data.stepSize)
				.setScale(data.quantityPrecision).doubleValue();
	}

	private Double normalizePrice(BigDecimal price, String symbol) {
		SymbolData data = symbolData.get(symbol);
		return price.divide(data.tickSize, 0, RoundingMode.DOWN).multiply(data.tickSize)
				.setScale(data.pricePrecision).doubleValue();
	}

	private String normalizeSymbol(String symbol) {
		return symbol.replace(".P", "");
	}
	
	public ApiResponse<NewAlgoOrderResponse> stopLoss(WebhookData data) {
		final String symbol = normalizeSymbol(data.symbol());
		NewAlgoOrderRequest stopLossRequest = new NewAlgoOrderRequest()
				.symbol(symbol)
				.side(Side.SELL)
				.type("STOP_MARKET")
				.triggerPrice(normalizePrice(data.stopLoss(), symbol))
				.algoType("CONDITIONAL")
				.closePosition("true");
		return api.newAlgoOrder(stopLossRequest);
	}
	
	private class SymbolData {
		int pricePrecision;
		int quantityPrecision;
		BigDecimal tickSize;
		BigDecimal stepSize;
	}
}
