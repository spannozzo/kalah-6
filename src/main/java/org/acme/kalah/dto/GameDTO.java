package org.acme.kalah.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Game Creation Response",description = "response for when game is created")
public class GameDTO{
	@Schema(description = "game id", example = "123", required = true)
	String id;

	@Schema(description = "url for retrieving game", example = "http://<host>:<port>/games/1234", required = true)
	String uri;
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
