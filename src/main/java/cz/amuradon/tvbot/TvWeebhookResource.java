package cz.amuradon.tvbot;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/tvwebhook")
public class TvWeebhookResource {

	@POST
	public void handle(WebhookData data) {
		System.out.println(data);
	}
}
