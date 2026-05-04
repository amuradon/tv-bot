package cz.amuradon.tvbot;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/tvwebhook")
public class WebhookResource {

	private final String userUuid;
	
	private final ObjectMapper mapper;
	
	private final MutinyEmitter<WebhookData> webhookDataEmmitter;
	
	@Inject
	public WebhookResource(
			@ConfigProperty(name = "TVBOT_USER_UUID") String userUuid,
			ObjectMapper mapper,
			@Channel(WebhookProcessor.CHANNEL_NAME) final MutinyEmitter<WebhookData> webhookDataEmmitter) {
 		this.userUuid = userUuid;
		this.mapper = mapper;
		this.webhookDataEmmitter = webhookDataEmmitter;
	}
	
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
	@POST
	public RestResponse<Object> handle(String body) {
		Log.debugf("Received body %s", body);
		
		WebhookData data;
		try {
			// Due TradingView sending Content-Type: text/plain MIME type, I need to parse it myself
			data = mapper.readValue(body, WebhookData.class);
		} catch (JsonProcessingException e) {
			Log.errorf(e, "Not able to parse body as JSON: %s", body);
			return ResponseBuilder.create(Status.BAD_REQUEST).entity("Not able to parse body as JSON").build();
		}
		
		Log.infof("Received webhook request for %s, %s", data.symbol(), data.strategy());
		
		if (data.userUuid() == null || !data.userUuid().equals(userUuid)) {
			Log.errorf("Request for %s, %s unauthorized", data.symbol(), data.strategy());
			return ResponseBuilder.create(Status.UNAUTHORIZED).build();
		}

		if (webhookDataEmmitter.hasRequests()) {
			webhookDataEmmitter.sendAndForget(data);
			Log.debug("Emmitted webhook event");
		}
		
		return ResponseBuilder.ok().build();
	}
}
