package dev.jmjimenez.security_spring_boot.service.impl;

import java.time.Duration;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.jmjimenez.security_spring_boot.dto.auth.LoggedUserDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.LoginRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterRequestDTO;
import dev.jmjimenez.security_spring_boot.dto.auth.RegisterResponseDTO;
import dev.jmjimenez.security_spring_boot.entity.User;
import dev.jmjimenez.security_spring_boot.enums.Roles;
import dev.jmjimenez.security_spring_boot.exception.BadRequestException;
import dev.jmjimenez.security_spring_boot.exception.ResourceNotFoundException;
import dev.jmjimenez.security_spring_boot.repository.UserRepository;
import dev.jmjimenez.security_spring_boot.service.AuthService;
import dev.jmjimenez.security_spring_boot.utility.CsrfTokenUtil;
import dev.jmjimenez.security_spring_boot.utility.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JWTUtils jwtUtils;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	@Override
	public RegisterResponseDTO register(RegisterRequestDTO registerRequest, HttpServletRequest request, HttpServletResponse response) {
	    if (userRepository.existsByEmail(registerRequest.getEmail())) {
	        throw new BadRequestException("Email is already taken");
	    }

	    if (userRepository.existsByPhone(registerRequest.getPhone())) {
	        throw new BadRequestException("Phone number is already taken");
	    }

	    if (!registerRequest.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
	        throw new BadRequestException("Formato de email inválido");

	    User newUser = new User();
	    newUser.setName(registerRequest.getName());
	    newUser.setSurnames(registerRequest.getSurnames());
	    newUser.setEmail(registerRequest.getEmail());
	    newUser.setPhone(registerRequest.getPhone());
	    newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
	    newUser.setRoles(Collections.singleton(Roles.USER));
	    newUser.setTokenVersion(0);

	    userRepository.save(newUser);

	    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

	    String accessToken = jwtUtils.generateToken(userDetails, request);
	    String refreshToken = jwtUtils.generateRefreshToken(userDetails, request);
	    String csrfToken = CsrfTokenUtil.generateCsrfToken();

	    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
	            .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

	    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
	            .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofDays(7)).build();

	    ResponseCookie csrfCookie = ResponseCookie.from("csrf_token", csrfToken)
	            .httpOnly(false).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());

	    logger.debug("Usuario " + registerRequest + " creado correctamente");
	    return new RegisterResponseDTO("User registered successfully");
	}


	@Override
	public void login(LoginRequestDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
		// 1. Cargar usuario
		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

		// 2. Verificar contraseña
		if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
			throw new BadRequestException("Credenciales inválidas");
		}

		// 3.1. Invalidar tokens anteriores
		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
		user.setTokenVersion(user.getTokenVersion() + 1);
		userRepository.save(user);

		// 3.2. Generar tokens
		String accessToken = jwtUtils.generateToken(userDetails, request);
		String refreshToken = jwtUtils.generateRefreshToken(userDetails, request);
		String csrfToken = CsrfTokenUtil.generateCsrfToken();

		// 4. Cookies seguras
		ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken).httpOnly(true).secure(true)
				.sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

		ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(true)
				.sameSite("Strict").path("/").maxAge(Duration.ofDays(7)).build();

		// CSRF como cookie NO HttpOnly (visible al frontend)
		ResponseCookie csrfCookie = ResponseCookie.from("csrf_token", csrfToken).httpOnly(false).secure(true)
				.sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

		// 5. Añadir cookies a la respuesta
		response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());

		// 6. (Opcional) devolver estado 200 sin body
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public void refreshToken(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
	    String username = jwtUtils.extractUsername(refreshToken);
	    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

	    if (!jwtUtils.isValidToken(refreshToken, userDetails, request, "refresh")) {
	        throw new RuntimeException("Invalid or expired refresh token");
	    }

	    String accessToken = jwtUtils.generateToken(userDetails, request);
	    String newRefreshToken = jwtUtils.generateRefreshToken(userDetails, request);
	    String csrfToken = CsrfTokenUtil.generateCsrfToken();

	    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
	            .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

	    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
	            .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofDays(7)).build();

	    ResponseCookie csrfCookie = ResponseCookie.from("csrf_token", csrfToken)
	            .httpOnly(false).secure(true).sameSite("Strict").path("/").maxAge(Duration.ofHours(1)).build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());

	    response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public LoggedUserDTO getProfile() {
		// Obtener el usuario desde el contexto de seguridad
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// Buscar el usuario en la base de datos por username (o por email)
		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		return LoggedUserDTO.builder().name(user.getName()).surnames(user.getSurnames()).phone(user.getPhone())
				.email(user.getEmail()).roles(user.getRoles()).build();
	}
}