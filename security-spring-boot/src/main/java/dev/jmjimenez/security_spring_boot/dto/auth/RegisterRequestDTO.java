package dev.jmjimenez.security_spring_boot.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {

	private String name;
	private String surnames;
	private String email;
	private String phone;
	private String password;
	
}
