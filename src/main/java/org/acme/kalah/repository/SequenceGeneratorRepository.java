package org.acme.kalah.repository;

import java.util.Optional;

import org.acme.kalah.entity.DatabaseSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceGeneratorRepository extends MongoRepository<DatabaseSequence, String> {
	Optional<DatabaseSequence> findById(String id);
}
