package cz.amuradon.tvbot;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class MyTestProfile implements QuarkusTestProfile {

	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of("TVBOT_USER_UUID", "tvbot_user_uuid");
	}
}
