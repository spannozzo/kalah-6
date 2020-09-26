package org.acme.kalah.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.acme.kalah.entity.Game;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Game Status Response",description = "response describing the status of a retrieved game")
public class GameStatusDTO{
	@Schema(description = "game id", example = "123", required = true)
	String id;

	@Schema(description = "pits of the game", 
			example = "{\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"4\",\"5\":\"4\",\"6\":\"4\",\"7\""
			+ ":\"0\",\"8\":\"4\",\"9\":\"4\",\"10\":\"4\",\"11\":\"4\",\"12\":\"4\",\"13\":\"4\",\"14\":\"0\"}",
			required = true)
	
	Map<String, String> status;

	@Schema(description = "Next turn", allowableValues = {"Player 1", "Player 2"}, example = "Player 1", required = true )
	private String turn;
	
	@Schema(description = "Score", example = "0 — 0", required = true )
	private String score;
	
	@Schema(description = "Winner",allowableValues = {"Player 1", "Player 2",""}, example = "Player 2", required = true )
	private String winner;
	
	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public String getTurn() {
		return turn;
	}

	public void setTurn(String turn) {
		this.turn = turn;
	}

	public Map<String, String> getStatus() {
		return status;
	}

	public void setStatus(Map<String, String> status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public void map(Game game) {
		id=String.valueOf(game.getId());
		
		status=new LinkedHashMap<>(14);
		
		IntStream.range(1, Game.PITS_NUMBER+1)
		.forEach(i->
			status.put(String.valueOf(i), String.valueOf(game.getPits().get(i-1)))
        );
		turn=(game.getTurn().equals(true)?"Player 1":"Player 2");
		score=game.getKala1()+" — "+game.getKala2();
		
		winner="";
		
		if (game.getWinner()==1) {
			winner="Player 1";
		}else if (game.getWinner()==2) {
			winner="Player 2";
		}
		
		
		
	}
	
}
