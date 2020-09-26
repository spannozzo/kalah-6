package org.acme.kalah.controller;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import org.acme.kalah.dto.GameDTO;
import org.acme.kalah.dto.GameStatusDTO;
import org.acme.kalah.dto.MoveDTO;
import org.acme.kalah.entity.Game;
import org.acme.kalah.error.GameNotFoundException;
import org.acme.kalah.error.InvalidMoveException;
import org.acme.kalah.service.GameService;
import org.acme.kalah.service.MoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/games")
@Validated
public class GameController {

	@Autowired
	private GameService gameService;
	
	@Autowired
	private MoveService moveService;

	
	
	@Operation(summary = "create a game and return its id")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "game created", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GameDTO.class)) }) })

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GameDTO> gamePost() {

		Game game = gameService.create();

		return ResponseEntity.status(HttpStatus.CREATED).body(gameService.getGameDTO(game));
	}

	@Operation(summary = "get game by id and retrieves a rapresentation of its status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "game found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GameStatusDTO.class)) 
			}),
			@ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "game not found", content = @Content) })

	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GameStatusDTO> getGame(@PathVariable("id") @Positive Long  id)  {
						
		Game game=gameService.findById(id).orElseThrow(()->new GameNotFoundException("game not found for id "+id));
		
		GameStatusDTO gameResponse=new GameStatusDTO();
		
		gameResponse.map(game);
		
		return ResponseEntity.ok(gameResponse);
		
	}
	@Operation(summary = "make a move. It empties the selected pit for a specif game id, and perform the game by using Kalah rules")
	@ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "move done", 
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = MoveDTO.class)) 
        }),
        @ApiResponse(responseCode = "400", description = "Invalid game id supplied", content = @Content),
		@ApiResponse(responseCode = "404", description = "game not found", content = @Content) 
        })
	@PutMapping(path = "{gameId}/pits/{pitId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MoveDTO> makeAMove(@PathVariable("gameId") @Positive Long  gameId, 
			@PathVariable("pitId") @Min(value = 1) @Max(value = 14) Integer  pitId) throws InvalidMoveException {
		
		Game game=gameService.findById(gameId).orElseThrow(()->new GameNotFoundException("game not found for id "+gameId));
	
		game.validateMove(pitId);
				
		MoveDTO responseDTO=moveService.makeMove(game,pitId);
		
		return ResponseEntity.ok(responseDTO);
		
	}
}
