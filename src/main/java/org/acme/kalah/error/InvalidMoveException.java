package org.acme.kalah.error;

public class InvalidMoveException extends Exception {

	private static final long serialVersionUID = -6603841878839894817L;

	
	public InvalidMoveException(String message) {
		super(message);
	}
	
}
