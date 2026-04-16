package cz.amuradon.tvbot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ConnectionTest {

	@Disabled("NOT UNIT TEST, only to test GCP connection")
	@Test
	public void test() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.header("Content-Type", "application/json")
				.uri(URI.create("https://mesaibit.io/tvwebhook"))
				.POST(BodyPublishers.ofString(
						"""
						{
						    "symbol": "BTC",
						    "side": "buy",
						    "quantity": 0.01,
						    "reduceOnly": false,
						    "newClientOrderId": "someID",
						    "userUuid": "2da0b677-ff69-4682-ae37-f95e28e8dcb3"
						}
						"""))
				.build();
		HttpResponse<String> body = client.send(request, BodyHandlers.ofString());
		System.out.println(body);
	}
}
