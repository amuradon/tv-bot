package cz.amuradon.tvbot;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParsingTest {

	@Test
	public void test() throws JsonMappingException, JsonProcessingException {
		String json =
				"""
				{
				    "symbol": "SUIUSDT.P",
				    "side": "buy",
				    "quantity": 12.01,
				    "reduceOnly": "false",
				    "newClientOrderId": "localhost/someId",
				    "stopLoss": 0.254,
				    "userUuid": "2da0b677-ff69-4682-ae37-f95e28e8dcb3"
				}
				""";
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(json, WebhookData.class);
	}
}
