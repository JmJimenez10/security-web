package dev.jmjimenez.security_spring_boot.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.jmjimenez.security_spring_boot.service.impl.CustomUserDetailsService;
import dev.jmjimenez.security_spring_boot.utility.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);

	@Autowired
	private JWTUtils jwtUtils;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		final String jwtToken;
		final String userEmail;

		// Si no hay cabecera de Authorization, pasar al siguiente filtro
		if (authHeader == null || authHeader.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// Eliminar "Bearer" del token
			jwtToken = authHeader.substring(7);
			userEmail = jwtUtils.extractUsername(jwtToken);

			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

				// Verificar si el token es válido
				if (jwtUtils.isValidToken(jwtToken, userDetails, request, "access")) {
					// Si el token es válido, establecer el contexto de seguridad
					SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					securityContext.setAuthentication(token);
					SecurityContextHolder.setContext(securityContext);

				} else {
					logger.warn("Token inválido o expirado para el usuario: {}", userEmail);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
					response.setContentType("application/json");
					response.getWriter().write("{\"error\": \"Token inválido o expirado\"}");
					return; // Detener el flujo si el token no es válido
				}

			}
		} catch (Exception e) {
			logger.error("Error al procesar el token JWT", e);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 401 Unauthorized
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Error al procesar el token\"}");
			return;
		}
		
		// Continuar con la cadena de filtros
		filterChain.doFilter(request, response);
	}
}
