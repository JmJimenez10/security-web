package dev.jmjimenez.security_spring_boot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jmjimenez.security_spring_boot.entity.User;
import dev.jmjimenez.security_spring_boot.enums.Roles;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String username);
	
	boolean existsByEmail(String email);
	
	boolean existsByPhone(String phone);
	
	int countByRoles(Roles admin);
	
}
