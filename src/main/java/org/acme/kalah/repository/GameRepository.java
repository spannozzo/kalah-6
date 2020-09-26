package org.acme.kalah.repository;

import java.util.Optional;

import org.acme.kalah.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, String>{
	public Optional<Game> findById(Long id);
}
