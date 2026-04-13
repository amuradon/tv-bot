package cz.amuradon.tvbot;

import io.quarkus.logging.Log;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/tvwebhook")
public class TvWeebhookResource {

	@POST
	public void handle(WebhookData data) {
		Log.infof("Received webhook request %s", data);
	}
}
