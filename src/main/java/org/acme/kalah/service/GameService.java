package org.acme.kalah.service;

import java.util.Optional;

import org.acme.kalah.dto.GameDTO;
import org.acme.kalah.dto.MoveDTO;
import org.acme.kalah.entity.Game;


public interface GameService {

	Game create();
	Optional<Game> findById(Long id);
	String getGameGetURL(String id);
	void save(Game game);
	GameDTO getGameDTO(Game game);
	MoveDTO getMoveDTO(Game game);

}
