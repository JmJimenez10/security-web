package dev.jmjimenez.security_spring_boot.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class CsrfTokenUtil {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
	
	public static String generateCsrfToken() {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return base64Encoder.encodeToString(randomBytes);
	}
	
}
