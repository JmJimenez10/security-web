package dev.jmjimenez.security_spring_boot.config;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {

	private static final List<String> METHODS_TO_PROTECT = List.of("POST", "PUT", "DELETE", "PATCH");
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		
		String method = request.getMethod();
		
		// Excluir el endpoint de login de la protección CSRF
		if (request.getRequestURI().contains("/api/auth/")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		// Solo proteger los métodos que modifican datos
		if (METHODS_TO_PROTECT.contains(method)) {
			
			// Obtener el token CSRF desde la cookie
			String csrfTokenFromCookie = getCookieValue(request, "csrf_token");
			
			// Obtener el token CSRF desde el header
			String csrfTokenFromHeader = request.getHeader("X-CSRF-TOKEN");
			
			// Verificar si el token de la cabecera es válido
			if (csrfTokenFromHeader == null || !csrfTokenFromHeader.equals(csrfTokenFromCookie)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"CSRF token inválido o flatante\"}");
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}
	
	// Método auxiliar para obtener el valor de una cookie por su nombre
	private String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName))
					return cookie.getValue();
			}
		}
		
		return null;
	}
	
}
