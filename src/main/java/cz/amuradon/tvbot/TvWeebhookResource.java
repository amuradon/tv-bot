package cz.amuradon.tvbot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.net.ssl.HttpsURLConnection;

import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;

import io.quarkus.logging.Log;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/tvwebhook")
public class TvWeebhookResource {

	@POST
	public void handle(WebhookData data) {
		Log.infof("Received webhook request %s", data);
		
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
		         .uri(URI.create("https://ifconfig.me/ip"))
		         .build();
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			Log.infof("Response %d\n%s", response.statusCode(), response.body());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
