package org.acme.kalah.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class GameNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 6971201385792839L;
	
	public GameNotFoundException(String errorMessage){
		super(errorMessage);
	}
	
}
