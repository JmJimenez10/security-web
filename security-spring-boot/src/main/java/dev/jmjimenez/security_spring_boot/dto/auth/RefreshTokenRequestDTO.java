package dev.jmjimenez.security_spring_boot.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

	private String refreshToken;
	
}
