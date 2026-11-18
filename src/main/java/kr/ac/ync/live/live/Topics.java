package kr.ac.ync.live.live;

/**
 * Every STOMP destination lives here and nowhere else (the client only subscribes; the server broadcasts).
 * /topic is for broadcasting; /app is not used in this project.
 */
public final class Topics {

	public static final String WS_ENDPOINT = "/ws";
	public static final String BROKER_PREFIX = "/topic";
	public static final String APP_PREFIX = "/app";

	private Topics() {
	}

	public static String questions(String code) {
		return BROKER_PREFIX + "/sessions/" + code + "/questions";
	}

	public static String polls(String code) {
		return BROKER_PREFIX + "/sessions/" + code + "/polls";
	}

	public static String status(String code) {
		return BROKER_PREFIX + "/sessions/" + code + "/status";
	}
}
