package dev.jmjimenez.security_spring_boot.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

	private String accessToken;
	private String refreshToken;
	
}
