package org.acme.kalah.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.acme.kalah.entity.Game;
import org.acme.kalah.repository.GameRepository;
import org.acme.kalah.repository.SequenceGeneratorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.hamcrest.Matchers.notNullValue;

import static org.hamcrest.Matchers.emptyOrNullString;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class GameServiceTest {

	@Autowired
	GameService gameService;
	
	@Autowired
	GameRepository gameRepository;
	
	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;
		
	@Test
	@Order(1)	
	void game_should_be_saved_with_id_1() {
		Game game=gameService.create();
		
		assertThat(gameRepository.count(), is(1L));
		
		assertThat(game, is(notNullValue()));
		assertThat(game.getId(), is((1L)));
	}
	
	@Test
	@Order(2)	
	void game_should_be_saved_with_id_2() {
		Game game=gameService.create();
		
		assertThat(gameRepository.count(), is(2L));
		
		Assertions.assertNotNull(game);
		assertThat(game.getId(), is(2L));
	}
	@Test
	@Order(3)	
	void url_should_be_retrieved(){
		String gameId="1";
		String url=gameService.getGameGetURL(gameId);
		
		assertThat(url, is(not(emptyOrNullString())));
		assertThat(url.endsWith("/games/"+gameId), is(true));
				
	}
	
	@Test
	@Order(4)	
	void should_remove_all() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}

}
