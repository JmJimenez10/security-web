package dev.jmjimenez.security_spring_boot.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dev.jmjimenez.security_spring_boot.enums.Roles;
import dev.jmjimenez.security_spring_boot.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JWTAuthFilter jwtAuthFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e.authenticationEntryPoint(new Http401UnauthorizedEntryPoint())
						.accessDeniedHandler((req, res, ex) -> {
							res.setStatus(HttpServletResponse.SC_FORBIDDEN);
							res.setContentType("application/json");
							res.getWriter().write("");
						}))
				.authorizeHttpRequests(request -> request
						.requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token",
								"/public/**")
						.permitAll().requestMatchers("/api/admin/**").hasRole(Roles.ADMIN.name())
						.requestMatchers("/api/**").hasAnyAuthority(Roles.ADMIN.name(), Roles.USER.name()).anyRequest()
						.authenticated())

				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOrigins(List.of("http://localhost:5173", "https://webprod.com"));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
	    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-Token")); // Aquí añades 'X-CSRF-Token'
	    config.setAllowCredentials(true); // NECESARIO para cookies (HttpOnly)
	    config.setMaxAge(3600L); // Opcional: para cachear la configuración

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config); // Aplica a todas las rutas
	    return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
