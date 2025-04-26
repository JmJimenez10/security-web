package dev.jmjimenez.security_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {

	private String error;
	private String message;
	
}
