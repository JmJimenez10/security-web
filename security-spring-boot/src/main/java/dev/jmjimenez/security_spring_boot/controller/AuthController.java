package dev.jmjimenez.security_spring_boot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.jmjimenez.security_spring_boot.dto.auth.LoggedUserDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.LoginRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterResponseDTO;
import dev.jmjimenez.security_spring_boot.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	// Endpoint para el registro
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponseDTO register(@RequestBody RegisterRequestDTO registerRequest, HttpServletRequest request, HttpServletResponse response) {
		return authService.register(registerRequest, request, response);
	}

	// Endpoint para el login
	@PostMapping("/login")
	@ResponseStatus(HttpStatus .OK)
	public void login(@RequestBody LoginRequestDTO loginRequest,
	                  HttpServletRequest request,
	                  HttpServletResponse response) {
	    authService.login(loginRequest, request, response);
	}

	// Endpoint para refrescar el token
	@PostMapping("/refresh-token")
	@ResponseStatus(HttpStatus.OK)
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
	    // Obtener el refresh token de las cookies
	    String refreshToken = getCookieValue(request, "refresh_token");

	    if (refreshToken == null) {
	        throw new RuntimeException("No refresh token found in cookies");
	    }

	    // Llamar al servicio para hacer el refresh
	    authService.refreshToken(refreshToken, request, response);
	}

	// Endpoint para obtener el perfil del usuario autenticado
	@GetMapping("/profile")
	@ResponseStatus(HttpStatus.OK)
	public LoggedUserDTO getProfile() {
		return authService.getProfile();
	}
	
	private String getCookieValue(HttpServletRequest request, String cookieName) {
	    // Buscar en las cookies
	    if (request.getCookies() != null) {
	        for (Cookie cookie : request.getCookies()) {
	            if (cookie.getName().equals(cookieName)) {
	                return cookie.getValue();
	            }
	        }
	    }
	    return null; // Si no se encuentra, devolver null
	}
	
	// Endpoint para hacer logout
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	public void logout(HttpServletResponse response) {
	    authService.logout(response);
	}
}