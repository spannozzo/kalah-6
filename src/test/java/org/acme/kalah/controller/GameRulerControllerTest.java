package org.acme.kalah.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import org.acme.kalah.dto.GameStatusDTO;
import org.acme.kalah.entity.Game;
import org.acme.kalah.repository.GameRepository;
import org.acme.kalah.repository.SequenceGeneratorRepository;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class GameRulerControllerTest {

	private static String gameId;
	
	private static String finalScore;
	
	@LocalServerPort
    int randomServerPort;
	
	@Autowired
	GameRepository gameRepository;
	
	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |	___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 6 || 6 || 6 || 6 || 6 || 6 || 6 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 6 || 6 || 6 || 6 || 6 || 6 || 6 |  0 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(1)
	void should_create_a_game() {
		
		gameId=given()
				.port(randomServerPort)
				.when()
				.post("/games")
				.then()
		        	.statusCode(201)
		        	.assertThat().body("id", is(not(emptyString())))
		        	.assertThat().body("uri", is(not(emptyString())))
		    	.extract()
		        .path("id");
		        ;
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getPits().size(), is(14));
        
        game.getPits().parallelStream().forEach(tokens->assertThat(tokens, is(6)));
        
        assertThat(game.getKala1(), is(0));
        assertThat(game.getKala2(), is(0));
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	
	@Test
	@Order(2)
	void should_fail_when_pit_8_to_14_are_move_on_player_1_turn() {
		Random r = new Random();
		Integer pitId = r.ints(8, (14 + 1)).limit(1).findFirst().getAsInt();
		
		given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(400)
	    ;        
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |	___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 6 || 6 || 6 || 6 || 6 || 6 || 6 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	                                       |
	 * |	_______1____1____1____1____1____1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 7 || 7 || 7 || 7 || 7 || 7 |  0 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(3)
	void should_make_a_move_on_a_pit_1() {
		Integer pitId = 1;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
//      [0, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	
        	if(i==2 || i==3 || i==4 || i==5 || i==6 || i==7 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(7));	
        	}
        	else if (i!=pitId) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(6));	
        	}
        	
        });
        
        assertThat(game.getKala1(), is(0));
        assertThat(game.getKala2(), is(0));
       
        assertThat(game.getTurn(), is(false));
		
        assertThat(game.getWinner(), is(-1));
	}
	
	@Test
	@Order(4)
	void should_fail_when_move_on_empty_pit() {
				
		given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", 1)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(400)
	    ;
	}
	
	@Test
	@Order(4)
	void should_fail_when_pit_1_to_7_are_move_on_player_2_turn() {
		Random r = new Random();
		Integer pitId = r.ints(1, (7 + 1)).limit(1).findFirst().getAsInt();
		
		given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(400)
	    ;
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |	__1____1____1____1____1____1_______    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 7 || 7 || 7 || 7 || 7 || 7 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 7 || 7 || 7 || 7 || 7 || 7 |  0 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(5)
	void should_make_a_move_on_a_pit_8() {
		Integer pitId = 8;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(0));
        assertThat(game.getKala2(), is(0));
        
//      [0, 7, 7, 7, 7, 7, 7, 0, 7, 7, 7, 7, 7, 7]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==9 || i==10 || i==11 || i==12 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(7));	
        	}
        });
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |	___________________________1____1_     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 0	| 7 || 7 || 7 || 7 || 7 || 7 || 1 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	____________1____1____1____1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 8 || 8 || 8 || 8 || 8 |  1 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(6)
	void should_make_a_move_on_a_pit_2() {
		Integer pitId = 2;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(1));
        assertThat(game.getKala2(), is(0));
        
//      [0, 0, 8, 8, 8, 8, 8, 1, 8, 7, 7, 7, 7, 7]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==8) {
        		assertThat(game.getPits().get(i-1), is(1));	
        	}
        	if(i==3 || i==4 || i==5 || i==6 || i==7) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(8));	
        	}
        });
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * | 1  __1____1____1____1____1____________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 1	| 8 || 8 || 8 || 8 || 8 || 0 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 1 || 0 || 8 || 8 || 8 || 8 || 8 |  1 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(7)
	void should_make_a_move_on_a_pit_9() {
		Integer pitId = 9;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(1));
        assertThat(game.getKala2(), is(1));
        
//      [1, 0, 8, 8, 8, 8, 8, 1, 0, 8, 8, 8, 8, 8]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==1) {
        		assertThat(game.getPits().get(i-1), is(1));	
        	}
        	if(i==10 || i==11 || i==12 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(8));	
        	}
        });
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
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
	 */
	
	@Test
	@Order(8)
	void should_make_a_move_on_pit_1_and_steal_pit_13() {
		Integer pitId = 1;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(10));
        assertThat(game.getKala2(), is(1));
        
//      [0, 0, 8, 8, 8, 8, 8, 1, 0, 8, 8, 8, 0, 8]
        
        assertThat("pit 2 failed",game.getPits().get(1), is(0));	
        assertThat("pit 13 failed",game.getPits().get(12), is(0));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________1_______    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 10	| 8 || 0 || 8 || 8 || 8 || 0 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_______1__________________________   8 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 0 || 8 || 8 || 8 || 0 || 8 | 10 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	
	@Test
	@Order(9)
	void should_make_a_move_on_pit_8_and_steal_pit_6() {
		Integer pitId = 8;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(10));
        assertThat(game.getKala2(), is(10));
        
//      [0, 0, 8, 8, 8, 0, 8, 0, 0, 8, 8, 8, 0, 8]
        
        assertThat("pit 9 failed",game.getPits().get(8), is(0));	
        assertThat("pit 6 failed",game.getPits().get(5), is(0));	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    _________________1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 10	| 8 || 0 || 8 || 9 || 9 || 1 || 1 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	______________________1____1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 8 || 0 || 9 || 1 || 9 | 11 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	@Test
	@Order(10)
	void should_make_a_move_on_pit_4() {
		Integer pitId = 4;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(11));
        assertThat(game.getKala2(), is(10));
        
//      [0, 0, 8, 0, 9, 1, 9, 1, 1, 9, 9, 8, 0, 8]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==6 || i==8 || i==9) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==5 || i==7 || i==10 || i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(9));	
        	}
        });	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1  _1____1____1____1_________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 11	| 9 || 1 || 9 || 10|| 0 || 1 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1_________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 1 || 1 || 9 || 1 || 9 || 1 || 9 | 11 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	@Test
	@Order(11)
	void should_make_a_move_on_pit_10() {
		Integer pitId = 10;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(11));
        assertThat(game.getKala2(), is(11));
        
//      [1, 1, 9, 1, 9, 1, 9, 1,1, 0, 10, 9, 1, 9]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(10));	
        	}
        	if(i==13 || i==1 || i==2 || i==4) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==12 || i==14 || i==3) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(9));	
        	}
        });	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    _______1____1____1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 11	| 9 || 2 || 10|| 11|| 1 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 1 || 1 || 9 || 1 || 0 || 2 || 10| 12 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	@Test
	@Order(12)
	void should_make_a_move_on_pit_5() {
		Integer pitId = 5;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(12));
        assertThat(game.getKala2(), is(11));
        
//      [1, 1, 9, 1, 0, 2, 10, 2,2, 1, 11, 10, 2, 9]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==7 || i==12) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(10));	
        	}
        	if(i==6 || i==8 || i==9 || i==13) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==10) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(11));	
        	}
        });	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1  _1________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 12	| 10|| 0 || 10|| 11|| 1 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 1 || 1 || 9 || 1 || 0 || 2 || 10| 12 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	
	@Test
	@Order(13)
	void should_make_a_move_on_pit_13_and_mantain_turn() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(12));
        assertThat(game.getKala2(), is(12));
        
//      [1, 1, 9, 1, 0, 2, 10, 2,2, 1, 11, 10, 0, 10]
        
        assertThat("pit 14 failed",game.getPits().get(13), is(10));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1  __________________________1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 13	| 0 || 0 || 10|| 11|| 1 || 3 || 3 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1____1____1____1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 2 || 2 || 10|| 2 || 1 || 3 || 11| 12 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(14)
	void should_make_a_move_on_pit_14() {
		Integer pitId = 14;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(12));
        assertThat(game.getKala2(), is(13));
        
//      [2, 2, 10, 2, 1, 3, 11, 3,3, 1, 11, 10, 0, 0]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==3) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(10));	
        	}
        	if(i==1 || i==2 || i==4 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==6 || i==8 || i==9 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==5) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==7) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(11));	
        	}
        });	
        	
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ____________1____1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 13	| 0 || 0 || 11|| 12|| 2 || 4 || 4 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_________________1____1____1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 2 || 2 || 0 || 3 || 2 || 4 || 12| 13 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(15)
	void should_make_a_move_on_pit_3() {
		Integer pitId = 3;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(13));
        assertThat(game.getKala2(), is(13));
        
//      [2, 2, 0, 3, 2, 4, 12, 4, 4, 2, 12, 11, 0, 0]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(12));	
        	}
        	if(i==5 || i==10) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==6 || i==8 || i==9 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(4));	
        	}
        	if(i==4) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==12) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(11));	
        	}
        });	
        	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1  _1____1________________________1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 14	| 1 || 1 || 0 || 12|| 2 || 4 || 5 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1____1____1____1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 3 || 3 || 1 || 4 || 3 || 5 || 13| 13 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(16)
	void should_make_a_move_on_pit_12() {
		Integer pitId = 12;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(13));
        assertThat(game.getKala2(), is(14));
        
//      [3, 3, 1, 4, 3, 5, 13, 5, 4, 2, 12, 0, 1, 1]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(12));	
        	}
        	if(i==10) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==4 || i==9 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(4));	
        	}
        	if(i==6 || i==8 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(5));	
        	}
        	if(i==1 || i==2 || i==5) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==3 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==7) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(13));	
        	}
        });	
        	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __1____1____1____1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 14	| 2 || 2 || 1 || 13|| 3 || 5 || 6 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1____1____________  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 4 || 4 || 2 || 5 || 4 || 5 || 0 | 14 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(17)
	void should_make_a_move_on_pit_7() {
		Integer pitId = 7;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(14));
        assertThat(game.getKala2(), is(14));
        
//      [4, 4, 2, 5, 4, 6, 0, 5, 5, 3, 13, 1, 2, 2]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==12) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==3 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==10) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==1 || i==2 || i==5 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(4));	
        	}
        	if(i==4 || i==9 ) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(5));	
        	}
        	if(i==8) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(6));	
        	}
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(13));	
        	}
        });	
        	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1  _1________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 15	| 3 || 0 || 1 || 13|| 3 || 5 || 6 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 4 || 4 || 2 || 5 || 4 || 5 || 0 | 14 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(18)
	void should_make_a_move_on_pit_13_and_move_again() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(14));
        assertThat(game.getKala2(), is(15));
        
//      [4, 4, 2, 5, 4, 5, 0, 6, 5, 3, 13, 1, 0, 3]
        
        assertThat(game.getPits().get(13), is(3));
        	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    _______1___________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 20	| 3 || 0 || 0 || 13|| 3 || 5 || 6 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 4 || 0 || 2 || 5 || 4 || 5 || 0 | 14 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(19)
	void should_make_a_move_on_pit_12_and_steal_pit_2() {
		Integer pitId = 12;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(14));
        assertThat(game.getKala2(), is(20));
        
//      [4, 0, 2, 5, 4, 5, 0, 6, 5, 3, 13, 0, 0, 3]
        
        assertThat(game.getPits().get(12), is(0));
        assertThat(game.getPits().get(1), is(0));	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ________________________________1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 20	| 3 || 0 || 0 || 13|| 3 || 5 || 7 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(20)
	void should_make_a_move_on_pit_5_2() {
		Integer pitId = 5;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(20));
        
//      [4, 0, 2, 5, 0, 6, 1, 7, 5, 3, 13, 0, 0, 3]
        
        assertThat(game.getPits().get(5), is(6));
        assertThat(game.getPits().get(6), is(1));	
        assertThat(game.getPits().get(7), is(7));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1____1____1____1____1_______    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 21	| 4 || 1 || 1 || 14|| 4 || 6 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(21)
	void should_make_a_move_on_pit_8_and_move_again() {
		Integer pitId = 8;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(21));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 6, 4, 14, 1, 1, 4]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==12 || i==13) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==10 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(4));	
        	}
        	if(i==9) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(6));	
        	}
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(14));	
        	}
        });
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1____1____1____1____________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 22	| 5 || 2 || 2 || 15|| 5 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(22)
	void should_make_a_move_on_pit_9_and_move_again() {
		Integer pitId = 9;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(22));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 0, 5, 15, 2, 2, 5]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==12 || i==13) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==10 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(5));	
        	}
        	if(i==11) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(15));	
        	}
        });
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 23	| 6 || 0 || 2 || 15|| 5 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(23)
	void should_make_a_move_on_pit_13_and_move_again_2() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(23));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 0, 5, 15, 2, 0, 6]
        
        assertThat(game.getPits().get(13), is(6));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1____1____1_________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 24	| 7 || 1 || 3 || 16|| 0 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(24)
	void should_make_a_move_on_pit_10_and_move_again() {
		Integer pitId = 10;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(24));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 0, 0, 16, 3, 1, 7]
       
        assertThat(game.getPits().get(10), is(16));
        assertThat(game.getPits().get(11), is(3));
        assertThat(game.getPits().get(12), is(1));
        assertThat(game.getPits().get(13), is(7));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1__________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 25	| 8 || 2 || 0 || 16|| 0 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(25)
	void should_make_a_move_on_pit_12_and_move_again() {
		Integer pitId = 12;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(25));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 0, 0, 16, 0, 2, 8]
       
        assertThat(game.getPits().get(12), is(2));
        assertThat(game.getPits().get(13), is(8));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1_______________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 26	| 9 || 0 || 0 || 16|| 0 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 4 || 0 || 2 || 5 || 0 || 6 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(26)
	void should_make_a_move_on_pit_13_and_move_again_3() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(26));
        
//      [4, 0, 2, 5, 0, 6, 1, 0, 0, 0, 16, 0, 0, 9]
       
        assertThat(game.getPits().get(13), is(9));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 ________________________________1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 30	| 0 || 0 || 0 || 16|| 0 || 0 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1____1____1____1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 5 || 1 || 3 || 6 || 1 || 7 || 0 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(27)
	void should_make_a_move_on_pit_14_and_steal_pit_7() {
		Integer pitId = 14;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(30));
        
//      [5, 1, 3, 6, 1, 7, 0, 0, 0, 0, 16, 0, 0, 0]
       
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==7 || i==8) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(0));	
        	}
        	if(i==2 || i==5) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==3) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==1) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(5));	
        	}
        	if(i==4) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(6));	
        	}
        	if(i==6) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(7));	
        	}
        });	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 30	| 0 || 0 || 0 || 16|| 0 || 0 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_________________1____1____1_______    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 5 || 1 || 0 || 7 || 2 || 8 || 0 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(28)
	void should_make_a_move_on_pit_3_2() {
		Integer pitId = 3;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(30));
        
//      [5, 1, 0, 7, 2, 8, 0, 0, 0, 0, 16, 0, 0, 0]
       
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11*   10    9    8     |
	 * | 1  __1____1____2____1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 31	| 1 || 1 || 2 || 1 || 1 || 1 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1____1____1____1____1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 6 || 2 || 1 || 8 || 3 || 9 || 1 | 15 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(29)
	void should_make_a_move_on_pit_11() {
		Integer pitId = 11;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("1"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(15));
        assertThat(game.getKala2(), is(31));
        
//      [6, 2, 1, 8, 3, 9, 1, 1, 1, 1, 1, 2, 1, 1]
       
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	
        	if(i==3 || i==7 || i==8 || i==9 || i==10 || i==11 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==2 || i==12) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==5) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==1) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(6));	
        	}
        	if(i==4) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(8));	
        	}
        	if(i==6) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(9));	
        	}
        });	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 31	| 1 || 1 || 2 || 1 || 1 || 1 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 6 || 2 || 1 || 8 || 3 || 9 || 0 | 16 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(30)
	void should_make_a_move_on_pit_7_and_move_again() {
		Integer pitId = 7;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(16));
        assertThat(game.getKala2(), is(31));
        
        
//      [6, 2, 1, 8, 3, 9, 0, 1, 1, 1, 1, 2, 1, 1]       
        
        assertThat(game.getPits().get(6), is(0));	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 31	| 1 || 1 || 2 || 1 || 1 || 1 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________1____1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 6 || 2 || 1 || 8 || 0 || 10|| 1 | 17 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(30)
	void should_make_a_move_on_pit_5_and_move_again() {
		Integer pitId = 5;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(17));
        assertThat(game.getKala2(), is(31));
        
//      [6, 2, 1, 8, 0, 10, 1, 1, 1, 1, 1, 2, 1, 1]       
        
        assertThat(game.getPits().get(5), is(10));
        assertThat(game.getPits().get(6), is(1));	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 31	| 1 || 1 || 2 || 1 || 1 || 1 || 1 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 6 || 2 || 1 || 8 || 0 || 10|| 0 | 18 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(31)
	void should_make_a_move_on_pit_7_and_move_again_2() {
		Integer pitId = 7;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(18));
        assertThat(game.getKala2(), is(31));
        
//      [6, 2, 1, 8, 0, 10, 0, 1, 1, 1, 1, 2, 1, 1]       
       
        assertThat(game.getPits().get(6), is(0));	
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __1____1____1____1____1____1____1__    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 31	| 2 || 2 || 3 || 2 || 2 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1_____________________________1__  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 7 || 2 || 1 || 8 || 0 || 0 || 1 | 19 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(32)
	void should_make_a_move_on_pit_6() {
		Integer pitId = 6;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(19));
        assertThat(game.getKala2(), is(31));
        
//      [7, 2, 1, 8, 0, 10, 1, 2, 2, 2, 2, 3, 2, 2]           
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	if(i==3 || i==7) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(1));	
        	}
        	if(i==2 || i==8 || i==9 || i==10 || i==11 || i==13 || i==14) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(2));	
        	}
        	if(i==12) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(3));	
        	}
        	if(i==1) {
        		assertThat("pit "+i+" failed",game.getPits().get(i-1), is(7));	
        	}
        	
        });		
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 32	| 3 || 0 || 3 || 2 || 2 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 7 || 2 || 1 || 8 || 0 || 0 || 1 | 19 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(33)
	void should_make_a_move_on_pit_13_and_move_again_4() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(19));
        assertThat(game.getKala2(), is(32));
        
//      [7, 2, 1, 8, 0, 0, 1, 2, 2, 2, 2, 3, 0, 3]      
        
        assertThat(game.getPits().get(13), is(3));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1___________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 33	| 4 || 1 || 0 || 2 || 2 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 7 || 2 || 1 || 8 || 0 || 0 || 1 | 19 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(34)
	void should_make_a_move_on_pit_12_and_move_again_2() {
		Integer pitId = 12;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(19));
        assertThat(game.getKala2(), is(33));
        
//      [7, 2, 1, 8, 0, 0, 1, 2, 2, 2, 2, 0, 1, 4]       
        
        assertThat(game.getPits().get(12), is(1));
        assertThat(game.getPits().get(13), is(4));	
        
        assertThat(game.getTurn(), is(false));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 34	| 0 || 1 || 0 || 2 || 2 || 2 || 2 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	__1____1____1______________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 8 || 3 || 2 || 8 || 0 || 0 || 0 | 19 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(34)
	void should_make_a_move_on_pit_14_2() {
		Integer pitId = 14;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(19));
        assertThat(game.getKala2(), is(34));
        
//      [8, 3, 2, 8, 0, 0, 1, 2, 2, 2, 2, 0, 1, 0]        
        
        assertThat(game.getPits().get(0), is(8));
        assertThat(game.getPits().get(1), is(3));	
        assertThat(game.getPits().get(2), is(2));
       
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 34	| 0 || 1 || 0 || 2 || 2 || 2 || 2 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 8 || 3 || 2 || 8 || 0 || 0 || 0 | 20 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(35)
	void should_make_a_move_on_pit_7_and_move_again_3() {
		Integer pitId = 7;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(20));
        assertThat(game.getKala2(), is(34));
        
//      [8, 3, 2, 8, 0, 0, 0, 2, 2, 2, 2, 0, 1, 0]           
        
        assertThat(game.getPits().get(6), is(0));
                
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 34	| 0 || 1 || 0 || 2 || 0 || 2 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_________________1____1____________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 8 || 3 || 0 || 9 || 0 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(36)
	void should_make_a_move_on_pit_3_and_steal_pit_10() {
		Integer pitId = 3;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(34));
        
//      [8, 3, 0, 9, 0, 0, 0, 2, 2, 0, 2, 0, 1, 0]        
        
        assertThat(game.getPits().get(9), is(0));
        assertThat(game.getPits().get(4), is(0));	
        assertThat(game.getPits().get(3), is(9));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __1_______________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 2 || 0 || 2 || 2 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 3 || 0 || 9 || 0 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(37)
	void should_make_a_move_on_pit_13_and_steal_pit_1() {
		Integer pitId = 13;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 3, 0, 9, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0]
        
        assertThat(game.getPits().get(13), is(0));
        assertThat(game.getPits().get(0), is(0));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __________________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 2 || 0 || 2 || 2 |	T  |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	____________1____1____1____________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 1 || 10|| 1 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(38)
	void should_make_a_move_on_pit_2_without_stealing() {
		Integer pitId = 2;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 0, 1, 10, 1, 0, 0, 2, 2, 0, 2, 0, 0, 0]
        
        assertThat(game.getPits().get(2), is(1));
        assertThat(game.getPits().get(3), is(10));
        assertThat(game.getPits().get(4), is(1));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    _________________1____1___________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 3 || 1 || 0 || 2 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 0 || 1 || 10|| 1 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(39)
	void should_make_a_move_on_pit_9() {
		Integer pitId = 9;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 0, 1, 10, 1, 10, 0, 2, 0, 1, 3, 0, 0, 0]
        
        assertThat(game.getPits().get(9), is(1));
        assertThat(game.getPits().get(10), is(3));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __________________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 3 || 1 || 0 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	_________________1_________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 0 || 11|| 1 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(40)
	void should_make_a_move_on_pit_3_3() {
		Integer pitId = 3;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 0, 0, 11, 1, 0, 0, 2, 0, 1, 3, 0, 0, 0]
        
        assertThat(game.getPits().get(3), is(11));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    _________________1________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 4 || 0 || 0 || 2 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 0 || 0 || 11|| 1 || 0 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(41)
	void should_make_a_move_on_pit_10_2() {
		Integer pitId = 10;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 0, 0, 11, 1, 0, 0, 2, 0, 0, 4, 0, 0, 0]
        
        assertThat(game.getPits().get(10), is(4));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __________________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 43	| 0 || 0 || 0 || 4 || 0 || 0 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________1_______    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 0 || 11|| 0 || 1 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(42)
	void should_make_a_move_on_pit_5_without_stealing() {
		Integer pitId = 5;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(43));
        
//      [0, 0, 0, 11, 0, 1, 0, 2, 0, 0, 4, 0, 0, 0]
        
        assertThat(game.getPits().get(5), is(1));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __1____1____1_____________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 44	| 1 || 1 || 1 || 0 || 0 || 0 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 0 || 11|| 0 || 1 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(43)
	void should_make_a_move_on_pit_11_and_move_again() {
		Integer pitId = 11;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(44));
        
//      [0, 0, 0, 11, 0, 1, 0, 2, 0, 0, 0, 1, 1, 1]
        
        assertThat(game.getPits().get(13), is(1));
        assertThat(game.getPits().get(12), is(1));
        assertThat(game.getPits().get(11), is(1));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |  1 __________________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 45	| 0 || 1 || 1 || 0 || 0 || 0 || 2 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 0 || 11|| 0 || 1 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(44)
	void should_make_a_move_on_pit_14_and_move_again() {
		Integer pitId = 14;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(45));
        
//      [0, 0, 0, 11, 0, 1, 0, 2, 0, 0, 0, 1, 1, 0]
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    ______________________1____1______     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 45	| 0 || 1 || 1 || 0 || 1 || 1 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  T	| 0 || 0 || 0 || 11|| 0 || 1 || 0 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(45)
	void should_make_a_move_on_pit_8() {
		Integer pitId = 8;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(45));
        
//      [0, 0, 0, 11, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0]
        
        assertThat(game.getPits().get(8), is(1));
        assertThat(game.getPits().get(9), is(1));
        
        assertThat(game.getWinner(), is(-1));
        assertThat(game.getTurn(), is(true));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * |    __________________________________     | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 45	| 0 || 1 || 1 || 0 || 1 || 1 || 0 |	 T |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	________________________________1__    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * |  	| 0 || 0 || 0 || 11|| 0 || 0 || 1 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(46)
	void should_make_a_move_on_pit_6_2() {
		Integer pitId = 6;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(45));
        
//      [0, 0, 0, 11, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0]
        
        assertThat(game.getPits().get(6), is(1));
        
        assertThat(game.getWinner(), is(-1));        
        assertThat(game.getTurn(), is(false));
		
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * | 12 _________________1_________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 57	| 0 || 1 || 1 || 0 || 0 || 1 || 0 |	   |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________    |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | T	| 0 || 0 || 0 || 0 || 0 || 0 || 1 | 23 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(47)
	void should_make_a_move_on_pit_10_and_steal_pit_4() {
		Integer pitId = 10;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(23));
        assertThat(game.getKala2(), is(57));
        
//      [0, 0, 0, 11, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0]
        
        assertThat(game.getPits().get(6), is(1));
        
        assertThat(game.getTurn(), is(true));
        assertThat(game.getWinner(), is(-1));
	}
	/*
	 *  ___________________________________________
	 * |										   |
	 * |     14   13   12   11   10    9    8      |
	 * | 3  ___________________________________    | 	
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 60	| 0 || 0 || 0 || 0 || 0 || 0 || 0 |	 W |
	 * |	|___||___||___||___||___||___||___|    |
	 * |										   |
	 * |	___________________________________  1 |
	 * |	|	||	 ||	  ||   ||	||	 ||   |    |
	 * | 	| 0 || 0 || 0 || 0 || 0 || 0 || 0 | 24 |
	 * |	|___||___||___||___||___||___||___|    |
	 * |	  1	   2    3    4    5    6    7      |
	 * |___________________________________________|
	 */
	@Test
	@Order(48)
	void should_make_a_move_on_pit_7_and_finish_the_game_with_player_2_as_winner() {
		Integer pitId = 7;
		
		Map<String, String> status= given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", pitId)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
			        .extract().path("status")
			        ;
		assertThat(status, is(notNullValue()));
		assertThat(status.get(String.valueOf(pitId)), is("0"));
		
		Optional<Game> optGame=gameRepository.findById(Long.parseLong(gameId));
        assertThat(optGame.get(), is(notNullValue()));
        
        Game game=optGame.get();
        
        assertThat(game.getKala1(), is(24));
        assertThat(game.getKala2(), is(60));
        
//      [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        
        IntStream.range(1, game.getPits().size()+1)
        .parallel()
        .forEach(i->{
        	assertThat("pit "+i+" failed",game.getPits().get(i-1), is(0));
        });		
        
        assertThat(game.getWinner(), is(2));
        assertThat(game.isFinished(), is(true));
		
       finalScore=game.getKala1()+" — "+game.getKala2();
        
	}
	@Test
	@Order(49)
	void check_if_game_status_from_get_call_is_correct() {
				
		GameStatusDTO responseDTO=given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.when()
			.get("/games/{gameId}")
			.then()
	        	.statusCode(200)
	        	.assertThat().body("id", is(not(emptyString())))
	        	.assertThat().body("url", is(not(emptyString())))
	        	.assertThat().body("status", is(not(emptyArray())))
	        
	        .extract().as(GameStatusDTO.class);
			        
		assertThat(responseDTO, is(notNullValue()));
		assertThat(responseDTO.getWinner(), is("Player 2"));
		assertThat(responseDTO.getScore(), is(finalScore));
	}
	@Test
	@Order(100)	
	void should_remove_all() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}
	
	
}
