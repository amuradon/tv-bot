package cz.amuradon.tvbot;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponse;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponseSymbolsInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.ExchangeInformationResponseSymbolsInnerFiltersInner;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

//@QuarkusTest
//@TestProfile(MyTestProfile.class)
public class ComponentTests {

	@InjectMock
	DerivativesTradingUsdsFuturesRestApi apiMock;
	
	@Mock
	private ApiResponse<ExchangeInformationResponse> exchangeInfoResponseMock;

	@Mock
	private ExchangeInformationResponse exchangeInfoDataMock;
	
	@Mock
	private ExchangeInformationResponseSymbolsInner symbolMock;
	
	@Mock
	private ExchangeInformationResponseSymbolsInnerFiltersInner priceFilterMock;
	
	@Mock
	private ExchangeInformationResponseSymbolsInnerFiltersInner marketLotFilterMock;
	
	private AutoCloseable mocks;

	@BeforeEach
	public void beforeAll() {
		// In QuarkusTest the annotation @ExtendWith(MockitoExtension.class) does not work, so doing manual way
		mocks = MockitoAnnotations.openMocks(this);
				
		when(apiMock.exchangeInformation()).thenReturn(exchangeInfoResponseMock);
		when(exchangeInfoResponseMock.getData()).thenReturn(exchangeInfoDataMock);
		when(exchangeInfoDataMock.getSymbols()).thenReturn(List.of(symbolMock));
		
		when(symbolMock.getSymbol()).thenReturn("SUIUSDT");
		when(symbolMock.getStatus()).thenReturn("TRADING");
		when(symbolMock.getContractType()).thenReturn("PERPETUAL");
		when(symbolMock.getPricePrecision()).thenReturn(2l);
		when(symbolMock.getQuantityPrecision()).thenReturn(2l);
		when(symbolMock.getFilters()).thenReturn(List.of(priceFilterMock, marketLotFilterMock));
		
		when(priceFilterMock.getFilterType()).thenReturn("PRICE_FILTER");
		when(priceFilterMock.getTickSize()).thenReturn("0.01000");
		
		when(marketLotFilterMock.getFilterType()).thenReturn("MARKET_LOT_SIZE");
		when(marketLotFilterMock.getStepSize()).thenReturn("0.01");
	}
	
	@AfterEach
	public void closeMocks() throws Exception {
		mocks.close();
	}
	
	// TODO dokoncit testy
	//@Test
	public void errorTest() {
		when(apiMock.newOrder(any())).thenThrow(new ApiException(400, "", Collections.emptyMap(),
				"{\"code\":-1111,\"msg\":\"Precision is over the maximum defined for this asset.\"}"));
		given()
			.when().body(
					"""
					{"symbol":"SUIUSDT.P","side":"buy","quantity":5.192,"reduceOnly":false,
					"stopLoss": 105.02,"newClientOrderId": "localhost/someID",
					"userUuid":"tvbot_user_uuid"}
					""")
			.post("/tvwebhook")
			.then()
			// TODO ted to pada, neni tam osetreni chyby
				.statusCode(200);
	}
}
