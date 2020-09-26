package org.acme.kalah.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.emptyOrNullString;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.acme.kalah.repository.GameRepository;
import org.acme.kalah.repository.SequenceGeneratorRepository;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class GameControllerTest {

	private static String gameId;
	
	@LocalServerPort
    int randomServerPort;
	
	@Autowired
	GameRepository gameRepository;
	
	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;

	private static Integer pitId;
	
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
		
	}
	
	@Test
	@Order(2)
	void should_retrieve_a_game() {
		String retrievedGameStatus=
				given()
					.port(randomServerPort)
					.pathParam("id", gameId)
					.when()
					.get("/games/{id}")
					.then()
			        	.statusCode(200)
			        .extract().asString()
				        	
			    	;
    	
    	
    	assertThat(retrievedGameStatus, is(not(emptyOrNullString())));
	}
	@Test
	@Order(3)
	void should_make_a_move_on_a_random_pit() {
		Random r = new Random();
		pitId = r.ints(1, (7 + 1)).limit(1).findFirst().getAsInt();
		
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
	}
	@Test
	@Order(3)
	void should_fail_when_move_on_pit_with_0_tokens() {
				
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
	@Test
	@Order(3)
	void should_fail_when_pid_id_is_smaller_than_1() {
				
		given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", 0)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(400)
	        ;
	}
	@Test
	@Order(3)
	void should_fail_when_pid_id_is_bigger_than_14() {
				
		given()
			.port(randomServerPort)
			.pathParam("gameId", gameId)
			.pathParam("pitId", 15)
			.when()
			.put("/games/{gameId}/pits/{pitId}")
			.then()
	        	.statusCode(400)
	        ;
	}
	@Test
	@Order(4)	
	void should_remove_all() {
		
		sequenceGeneratorRepository.deleteAll();
		gameRepository.deleteAll();
		
		assertThat(gameRepository.count(), is(0L));
		assertThat(sequenceGeneratorRepository.count(), is(0L));
	}
	@Test
	@Order(5)
	void should_fail_with_404_when_retrieiving_an_unexistant_game() {
		
		given()
			.port(randomServerPort)
			.pathParam("id", gameId)
			.when()
			.get("/games/{id}")
			.then()
	        	.statusCode(HttpStatus.NOT_FOUND.value())
	    ;
	}
	
	@Test
	@Order(5)
	void should_fail_with_400_when_retrieiving_game_with_wrong_id() {
		
		given()
			.port(randomServerPort)
			.pathParam("id", -1L)
			.when()
			.get("/games/{id}")
			.then()
	        	.statusCode(HttpStatus.BAD_REQUEST.value())
	    ;
	}
	
	@Test
	@Order(6)
	void should_fail_with_404_when_make_a_move_on_unexistant_game() {
		
		given()
		.port(randomServerPort)
		.pathParam("gameId", 1)
		.pathParam("pitId", 1)
		.when()
		.put("/games/{gameId}/pits/{pitId}")
		.then()
        	.statusCode(HttpStatus.NOT_FOUND.value())
        ;
	}
	@Test
	@Order(6)
	void should_fail_with_400_when_make_a_move_with_wrong_game_id() {
		
		given()
		.port(randomServerPort)
		.pathParam("gameId", -1)
		.pathParam("pitId", 1)
		.when()
		.put("/games/{gameId}/pits/{pitId}")
		.then()
        	.statusCode(HttpStatus.BAD_REQUEST.value())
        ;
	}
	
}
