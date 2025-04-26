package dev.jmjimenez.security_spring_boot.dto.auth;

import java.util.Collection;

import dev.jmjimenez.security_spring_boot.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedUserDTO {

	private String name;
	private String surnames;
	private String email;
	private String phone;
	private Collection<Roles> roles;
	
}
