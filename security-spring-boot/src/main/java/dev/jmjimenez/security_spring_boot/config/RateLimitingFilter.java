package dev.jmjimenez.security_spring_boot.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	@Value("${ratelimit.limit.login}")
	private int limitLogin;

	@Value("${ratelimit.limit.general}")
	private int limitGeneral;

	@Value("${ratelimit.duration.general}")
	private Duration generalRefillDuration;

	@Value("${ratelimit.duration.login}")
	private Duration loginRefillDuration;

	@Value("${ratelimit.duration.ban}")
	private Duration banDuration;

	private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
	private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
	private final Map<String, Instant> bannedIps = new ConcurrentHashMap<>();

	// Bucket para endpoints generales (ej: 70 req/min)
	private Bucket createGeneralBucket() {
		Bandwidth limit = Bandwidth.classic(limitGeneral, Refill.intervally(limitGeneral, generalRefillDuration));
		return Bucket.builder().addLimit(limit).build();
	}

	// Bucket más estricto para login (ej: 5 intentos por 5 min)
	private Bucket createLoginBucket() {
		Bandwidth limit = Bandwidth.classic(limitLogin, Refill.intervally(limitLogin, loginRefillDuration));
		return Bucket.builder().addLimit(limit).build();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String ip = request.getRemoteAddr();
		String path = request.getRequestURI();
		boolean isLoginRequest = path.equals("/api/auth/login");

		// Verificar si la IP está actualmente baneada
		if (bannedIps.containsKey(ip)) {
			Instant banExpiresAt = bannedIps.get(ip);
			if (Instant.now().isBefore(banExpiresAt)) {
				Duration remaining = Duration.between(Instant.now(), banExpiresAt);
				long secondsRemaining = remaining.getSeconds();

				writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests",
						"Your IP has been temporarily blocked.", path, secondsRemaining);
				log.warn("IP temporarily blocked: " + ip + ". Remaining time (seconds): " + secondsRemaining);
				return;
			} else
				// Se terminó el baneo, quitarlo
				bannedIps.remove(ip);
		}

		Bucket bucket = isLoginRequest ? loginBuckets.computeIfAbsent(ip, k -> createLoginBucket())
				: generalBuckets.computeIfAbsent(ip, k -> createGeneralBucket());

		if (bucket.tryConsume(1))
			filterChain.doFilter(request, response);
		else {
			// Si es un intento fallido en login, aplicamos baneo
			ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
			if (probe.isConsumed()) {
			    filterChain.doFilter(request, response);
			} else {
			    if (isLoginRequest) {
			        Instant banExpiresAt = Instant.now().plus(banDuration);
			        bannedIps.put(ip, banExpiresAt);
			        Duration remaining = Duration.between(Instant.now(), banExpiresAt);
			        long secondsRemaining = remaining.getSeconds();

			        writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
			            "Too Many Requests",
			            "Your IP has been temporarily blocked.",
			            path,
			            secondsRemaining);
			    } else {
			        writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
			            "Too Many Requests",
			            "Too many requests - you are rate limited.",
			            path,
			            null);
			    }
			}
		}
	}

	private void writeErrorResponse(HttpServletResponse response, int status, String error, String message, String path, Long retryAfterSeconds) throws IOException {
	    response.setStatus(status);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");

	    if (retryAfterSeconds != null) {
	        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
	    }

	    String json = "{" +
	        "\"timestamp\":\"" + Instant.now().toString() + "\"," +
	        "\"status\":" + status + "," +
	        "\"error\":\"" + error + "\"," +
	        "\"message\":\"" + message + "\"," +
	        "\"path\":\"" + path + "\"";

	    if (retryAfterSeconds != null) {
	        json += ",\"retryAfterSeconds\":" + retryAfterSeconds;
	    }

	    json += "}";

	    response.getWriter().write(json);
	}
}