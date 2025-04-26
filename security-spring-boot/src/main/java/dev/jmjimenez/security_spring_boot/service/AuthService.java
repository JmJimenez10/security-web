package dev.jmjimenez.security_spring_boot.service;

import dev.jmjimenez.security_spring_boot.dto.auth.LoggedUserDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.LoginRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO, HttpServletRequest request, HttpServletResponse response);
	
	void login(LoginRequestDTO loginRequestDTO, HttpServletRequest request, HttpServletResponse response);
	
	void refreshToken(String refreshToken, HttpServletRequest request, HttpServletResponse response);
	
	LoggedUserDTO getProfile();
	
	void logout(HttpServletResponse response);
	
}
