package org.acme.kalah.dto;

import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Move Response", description = "it return game id, game url, and status of the pits")
public class MoveDTO {
	@Schema(description = "game id", example = "123", required = true)
	String id;
	
	@Schema(description = "url for retrieving game", example = "http://<host>:<port>/games/1234", required = true)
	String url;
	
	@Schema(description = "pits of the game", 
			example = "{\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"4\",\"5\":\"4\",\"6\":\"4\",\"7\""
			+ ":\"0\",\"8\":\"4\",\"9\":\"4\",\"10\":\"4\",\"11\":\"4\",\"12\":\"4\",\"13\":\"4\",\"14\":\"0\"}",
			required = true)
	Map<String, String> status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getStatus() {
		return status;
	}

	public void setStatus(Map<String, String> status) {
		this.status = status;
	}

}
