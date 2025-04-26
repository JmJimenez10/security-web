package dev.jmjimenez.security_spring_boot.exception;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = -4626470996514944890L;

	public BadRequestException(String message) {
		super(message);
	}
}
