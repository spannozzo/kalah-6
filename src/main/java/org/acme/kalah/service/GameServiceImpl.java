package org.acme.kalah.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.acme.kalah.dto.GameDTO;
import org.acme.kalah.dto.MoveDTO;
import org.acme.kalah.entity.Game;
import org.acme.kalah.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Service
public class GameServiceImpl implements GameService {

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	SequenceGeneratorService sequenceGeneratorService;
	
	@Override
	public Game create() {
		Game game=new Game();
		
		game.setId(sequenceGeneratorService.generateSequence(Game.SEQUENCE_NAME));
		
		gameRepository.save(game);
		
		return game;
	}

	@Override
	public String getGameGetURL(String id) {
		return MvcUriComponentsBuilder.fromController(getClass())
	            .path("games/{id}")
	            .buildAndExpand(id)
	            .toString();
		
	}

	@Override
	public Optional<Game> findById(Long id) {
		
		return gameRepository.findById(id);
	}

	@Override
	public void save(Game game) {
		gameRepository.save(game);
		
	}

	@Override
	public GameDTO getGameDTO(Game game) {
		GameDTO gameDTO = new GameDTO();
		
		gameDTO.setId(String.valueOf(game.getId()));		
        gameDTO.setUri(this.getGameGetURL(gameDTO.getId()));
        
		return gameDTO;
	}

	@Override
	public MoveDTO getMoveDTO(Game game) {
		Map<String, String> pits = new LinkedHashMap<>(14);
		
		MoveDTO moveDTO = new MoveDTO();

		moveDTO.setId(String.valueOf(game.getId()));
		moveDTO.setUrl(this.getGameGetURL(String.valueOf(game.getId())));
		
		IntStream.range(1, Game.PITS_NUMBER+1).forEach(i->
        	pits.put(String.valueOf(i), String.valueOf(game.getPits().get(i-1)))	
        );	
	
		moveDTO.setStatus(pits);
		
		return moveDTO;
	}

}
