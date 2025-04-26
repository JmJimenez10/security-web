package dev.jmjimenez.security_spring_boot.exception;

public class AuthenticationException extends RuntimeException {

	private static final long serialVersionUID = -928258897969813247L;

	public AuthenticationException(String message) {
		super(message);
	}
}
