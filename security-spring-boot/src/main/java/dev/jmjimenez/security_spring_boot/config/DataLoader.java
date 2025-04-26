package dev.jmjimenez.security_spring_boot.config;

import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dev.jmjimenez.security_spring_boot.entity.User;
import dev.jmjimenez.security_spring_boot.enums.Roles;
import dev.jmjimenez.security_spring_boot.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void run(String... args) throws Exception {
		if (userRepository.countByRoles(Roles.ADMIN) == 0 ) {
			User admin = new User();
			admin.setName("Admin");
			admin.setSurnames("Administrator");
			admin.setEmail("admin@example.com");
			admin.setPhone("000000000");
			admin.setPassword(passwordEncoder.encode("admin"));
			admin.setRoles(Collections.singleton(Roles.ADMIN));
			admin.setCreationDate(LocalDateTime.now());
			admin.setLastModifiedDate(LocalDateTime.now());
			userRepository.save(admin);
		}
	}
}
