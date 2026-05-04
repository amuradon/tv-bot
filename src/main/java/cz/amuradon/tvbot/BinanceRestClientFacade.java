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

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BinanceRestClientFacade {

	// Chinese symbol mapping
	private static final Map<String, String> SYMBOL_MAPPING = Map.of("BIANRENSHENGUSDT", "币安人生USDT");

	private final DerivativesTradingUsdsFuturesRestApi api;
	
	private final Map<String, String> unsupportedSymbols;

	private final Map<String, SymbolData> symbolData;

	@Inject
	public BinanceRestClientFacade(DerivativesTradingUsdsFuturesRestApi api) {
		this.api = api;
		this.unsupportedSymbols = new HashMap<>();
		symbolData = new HashMap<>();
	}
	
	@PostConstruct
	public void init() {
		Log.debug("Loading exchange information...");
		ApiResponse<ExchangeInformationResponse> response = api.exchangeInformation();
		for (ExchangeInformationResponseSymbolsInner symbol : response.getData().getSymbols()) {
			if (symbol.getContractType().equalsIgnoreCase("PERPETUAL")
					&& symbol.getStatus().equalsIgnoreCase("TRADING")) {
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
				symbolData.put(symbol.getSymbol(), item);
			} else if (!symbol.getContractType().equalsIgnoreCase("PERPETUAL")) {
				// TradiFi_Perpetual not supported in my region, I don't trade others
				unsupportedSymbols.put(symbol.getSymbol(), "contractType=" + symbol.getContractType());
			} else if (!symbol.getStatus().equalsIgnoreCase("TRADING")) {
				unsupportedSymbols.put(symbol.getSymbol(), "status=" + symbol.getStatus());
			} else {
				unsupportedSymbols.put(symbol.getSymbol(), "other");
			}
		}
		Log.debugf("Exchange information loaded %s", symbolData);
	}
	
	public ApiResponse<NewOrderResponse> newOrder(String symbol, String side, BigDecimal quantity, boolean reduceOnly,
			String newClientOrderId) {
		symbol = normalizeSymbol(symbol);
		NewOrderRequest newOrderRequest = new NewOrderRequest()
				.symbol(symbol)
				.side("buy".equalsIgnoreCase(side) ? Side.BUY : Side.SELL)
				.type("MARKET")
				.quantity(normalizeQuantity(quantity, symbol))
				.reduceOnly(reduceOnly ? "true" : "false")
				.newClientOrderId(newClientOrderId);
		
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
		String processed = symbol.replace(".P", "");
		
		// Map chinese symbols
		String symbolMapping = SYMBOL_MAPPING.get(processed);
		if (symbolMapping != null) {
			processed = symbolMapping;
		}
		
		return processed;
	}
	
	public ApiResponse<NewAlgoOrderResponse> stopLoss(String symbol, BigDecimal stopLoss) {
		symbol = normalizeSymbol(symbol);
		NewAlgoOrderRequest stopLossRequest = new NewAlgoOrderRequest()
				.symbol(symbol)
				.side(Side.SELL)
				.type("STOP_MARKET")
				.triggerPrice(normalizePrice(stopLoss, symbol))
				.algoType("CONDITIONAL")
				.closePosition("true");
		
		// TODO ExchangeInfo muze byt stare a dojde k chybe -> podle typu chyby obnovit a zkusit znovu
		return api.newAlgoOrder(stopLossRequest);
	}
	
	public String isSupported(String symbol) {
		return unsupportedSymbols.get(normalizeSymbol(symbol));
	}
	
	private class SymbolData {
		int pricePrecision;
		int quantityPrecision;
		BigDecimal tickSize;
		BigDecimal stepSize;
		
		@Override
		public String toString() {
			return String.format("{pricePrecision=%d, quantityPrecision=%d, tickSize=%s, stepSize=%s}",
					pricePrecision, quantityPrecision, tickSize, stepSize);
		}
	}

}
