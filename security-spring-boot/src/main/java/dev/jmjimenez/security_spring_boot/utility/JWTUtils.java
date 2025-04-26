package dev.jmjimenez.security_spring_boot.utility;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import dev.jmjimenez.security_spring_boot.entity.User;
import dev.jmjimenez.security_spring_boot.exception.ResourceNotFoundException;
import dev.jmjimenez.security_spring_boot.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTUtils {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access.expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh.expiration}")
	private long refreshTokenExpiration;

	private SecretKey key;

	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	public void init() {
		byte[] decodedKey = Base64.getDecoder().decode(secret);
		this.key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
	}

	public String generateToken(UserDetails userDetails, HttpServletRequest request) {
		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		String userIp = request.getHeader("X-Forwarded-For");
		if (userIp == null || userIp.isEmpty())
			userIp = request.getRemoteAddr();

		String userAgent = request.getHeader("User-Agent");

		HashMap<String, Object> claims = new HashMap<>();
		claims.put("roles", userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList());
		claims.put("tokenVersion", user.getTokenVersion());
		claims.put("ip", userIp);
		claims.put("agent", userAgent);

		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)).signWith(key).compact();
	}
	
	public String generateRefreshToken(UserDetails userDetails, HttpServletRequest request) {
		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		String userIp = request.getHeader("X-Forwarded-For");
		if (userIp == null || userIp.isEmpty())
			userIp = request.getRemoteAddr();

		String userAgent = request.getHeader("User-Agent");

		HashMap<String, Object> claims = new HashMap<>();
		claims.put("token_type", "refresh");
		claims.put("tokenVersion", user.getTokenVersion());
		claims.put("ip", userIp);
		claims.put("agent", userAgent);

		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration)).signWith(key).compact();
	}

	public String extractUsername(String token) {
		return extractClaims(token, Claims::getSubject);
	}

	private <T> T extractClaims(String token, Function<Claims, T> resolver) {
		return resolver.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
	}

	public boolean isValidToken(String token, UserDetails userDetails, HttpServletRequest request, String type) {
		String userIp = request.getHeader("X-Forward-For");
		if (userIp == null || userIp.isEmpty())
			userIp = request.getRemoteAddr();

		String userAgent = request.getHeader("User-Agent");

		final String username = extractUsername(token);
		Integer tokenVersion = extractTokenVersion(token);

		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

		return username.equals(userDetails.getUsername()) && !isTokenExpired(token)
				&& validateIpAndAgent(token, userIp, userAgent) && type.equals("refresh") ? isRefreshToken(token)
						: !isRefreshToken(token) && tokenVersion.equals(user.getTokenVersion());
	}
	
	private boolean validateIpAndAgent(String token, String userIp, String userAgent) {
		String tokenIp = extractClaims(token, claims -> (String) claims.get("ip"));
		String tokenAgent = extractClaims(token, claims -> (String) claims.get("agent"));
		
		return userIp.equals(tokenIp) && userAgent.equals(tokenAgent);
	}
	
	private boolean isTokenExpired(String token) {
		return extractClaims(token, Claims::getExpiration).before(new Date());
	}
	
	private boolean isRefreshToken(String token) {
		return "refresh".equals(extractClaims(token, claims -> (String) claims.get("token_type")));
	}
	
	private Integer extractTokenVersion(String token) {
		return extractClaims(token, claims -> (Integer) claims.get("tokenVersion"));
	}
}
