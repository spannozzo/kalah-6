package org.acme.kalah.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.acme.kalah.entity.Game;
import org.acme.kalah.error.InvalidMoveException;
import org.acme.kalah.repository.GameRepository;
import org.acme.kalah.repository.SequenceGeneratorRepository;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
@TestMethodOrder(OrderAnnotation.class)
class MoveServiceTest {

	@Autowired
	MoveService moveService;
	
	@Autowired
	GameService gameService;
	
	@Autowired
	GameRepository gameRepository;
	
	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;

	private static Game retrievedGame;
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 1	| 8 || 8 || 8 || 8 || 8 || 0 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 1 || 0 || 8 || 8 || 8 || 8 || 8 |  1 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	@Test
	@Order(1)	
	void init_kalah_situation1() {
		Game game=gameService.create();
		
		assertThat(game, is(notNullValue()));
		
		game.setKala1(1);
		game.setKala2(1);
		
		Integer[] array= {1, 0, 8, 8, 8, 8, 8, 1, 0, 8, 8, 8, 8, 8};
		
		game.setPits(Stream.of(array).collect(Collectors.toList()));
		
		gameService.save(game);
			
		Optional<Game> retrievedOptional=gameService.findById(game.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		retrievedGame=retrievedOptional.get();
		
		assertThat(game.getKala1(), is(retrievedGame.getKala1()));
		assertThat(game.getKala2(), is(retrievedGame.getKala2()));
		
		assertThat(game.getPits(), is(retrievedGame.getPits()));
		
		assertThat(game.getTurn(), is(true));
	}
	
	/*
	 *  ___________________________________________
	 * |										   | 
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 1	| 8 || 0 || 8 || 8 || 8 || 0 || 1 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_______1__________________________   8 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 8 || 8 || 8 || 8 || 8 | 10 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 *  
	 */
	@Test
	@Order(2)	
	void check_if_steel_rule_works() throws InvalidMoveException {
		int pitId=1;
		retrievedGame.validateMove(pitId);
		
		moveService.makeMove(retrievedGame,pitId);
		
		Optional<Game> retrievedOptional=gameRepository.findById(retrievedGame.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		Game updatedGame=retrievedOptional.get();
		
		assertThat(updatedGame.getKala1(), is(10));
		assertThat(updatedGame.getKala2(), is(1));
		
		assertThat(updatedGame.getPits().get(0), is(0));
		assertThat(updatedGame.getPits().get(1), is(0));
		assertThat(updatedGame.getPits().get(12), is(0));
		
		assertThat(updatedGame.getTurn(), is(false));
		
	}
	@Test
	@Order(3)
	void should_remove_all_1() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 1 || 6 || 6 || 6 || 6 || 6 || 6 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 6 || 6 || 6 || 6 || 6 || 6 || 6 |  0 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(4)	
	void init_kalah_situation2() {
		Game game=gameService.create();
				
		assertThat(game, is(notNullValue()));
		
		game.setTurn(false);
		
		game.setKala1(0);
		game.setKala2(0);
		
		Integer[] array= {6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1};
		
		game.setPits(Stream.of(array).collect(Collectors.toList()));
		
		gameService.save(game);
			
		Optional<Game> retrievedOptional=gameService.findById(game.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		retrievedGame=retrievedOptional.get();
		
		assertThat(game.getKala1(), is(retrievedGame.getKala1()));
		assertThat(game.getKala2(), is(retrievedGame.getKala2()));
		
		assertThat(game.getPits(), is(retrievedGame.getPits()));
		
		assertThat(game.getTurn(), is(false));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * | 1  ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 1	| 0 || 6 || 6 || 6 || 6 || 6 || 6 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 6 || 6 || 6 || 6 || 6 || 6 || 6 |  0 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(5)	
	void check_move_on_last_pit_kala2_is_incrementend_and_turn_is_mantained() throws InvalidMoveException {
		int pitId=14;
		
		retrievedGame.validateMove(pitId);
		
		moveService.makeMove(retrievedGame,pitId);
		
		Optional<Game> retrievedOptional=gameRepository.findById(retrievedGame.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		Game updatedGame=retrievedOptional.get();
		
		assertThat(updatedGame.getKala1(), is(0));
		assertThat(updatedGame.getKala2(), is(1));
		
		assertThat(updatedGame.getPits().get(13), is(0));
		assertThat(updatedGame.getPits().get(0), is(6));
		
		assertThat(updatedGame.getTurn(), is(false));
		
	}
	@Test
	@Order(6)
	void should_remove_all_2() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 0 || 0 || 0 || 5 || 5 || 0 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T 	| 0 || 0 || 0 || 0 || 0 || 0 || 1 | 9  |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(7)	
	void init_kalah_situation3() {
		Game game=gameService.create();
		
		assertThat(game, is(notNullValue()));
		
		game.setKala1(9);
		game.setKala2(0);
		
		game.setTurn(true);
		
		Integer[] array= {0, 0, 0, 0, 0, 0, 1, 1, 0, 5, 5, 0, 0, 0};
		
		game.setPits(Stream.of(array).collect(Collectors.toList()));
		
		gameService.save(game);
			
		Optional<Game> retrievedOptional=gameService.findById(game.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		retrievedGame=retrievedOptional.get();
		
		assertThat(game.getKala1(), is(retrievedGame.getKala1()));
		assertThat(game.getKala2(), is(retrievedGame.getKala2()));
		
		assertThat(game.getPits(), is(retrievedGame.getPits()));
		
		assertThat(game.getTurn(), is(true));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 11	| 0 || 0 || 0 || 0 || 0 || 0 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________ 1  |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T 	| 0 || 0 || 0 || 0 || 0 || 0 || 0 | 10 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(8)	
	void game_should_finish_and_winner_should_be_player_2() throws InvalidMoveException {
		int pitId=7;
		
		retrievedGame.validateMove(pitId);
		
		moveService.makeMove(retrievedGame,pitId);
		
		Optional<Game> retrievedOptional=gameRepository.findById(retrievedGame.getId());
		assertThat(retrievedOptional.isPresent(), is(true));
		
		Game updatedGame=retrievedOptional.get();
		
		assertThat(updatedGame.getKala1(), is(10));
		assertThat(updatedGame.getKala2(), is(11));
		
		assertThat(updatedGame.isFinished(), is(true));
		assertThat(updatedGame.getWinner(), is(2));
		
		IntStream.range(1, updatedGame.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	assertThat("pit "+i+" failed",updatedGame.getPits().get(i-1), is(0));	
        });	
		
		assertThat(updatedGame.getTurn(), is(true));
	}
	
	@Test
	@Order(9)
	void should_remove_all_3() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}

	
}
