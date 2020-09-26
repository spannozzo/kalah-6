package org.acme.kalah.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.acme.kalah.entity.Game;
import org.acme.kalah.repository.SequenceGeneratorRepository;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
@TestMethodOrder(OrderAnnotation.class)
class SequenceGeneratorServiceTest {

	@Autowired
	SequenceGeneratorService service;
	
	@Autowired
	SequenceGeneratorRepository repository;
	
	@Test
	@Order(1)	
	void sequence_should_be_1() {
		assertThat(service.generateSequence(Game.SEQUENCE_NAME), is(1L));
	}
	@Test
	@Order(2)	
	void sequence_should_be_2() {
		assertThat(service.generateSequence(Game.SEQUENCE_NAME), is(2L));
	}
	@Test
	@Order(3)	
	void sequence_should_be_3() {
		assertThat(service.generateSequence(Game.SEQUENCE_NAME), is(3L));
	}
	@Test
	@Order(4)	
	void parallel_sequence_should_be_5() throws InterruptedException {
		
		Runnable r1=()->{
			service.generateSequence(Game.SEQUENCE_NAME);
		};
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r1);
		
		Stream<Thread> streams=Stream.of(t1,t2);
		
		streams.parallel().forEach(t->t.start());
		
		t1.join();
		t2.join();
		
		assertThat(repository.findById(Game.SEQUENCE_NAME).get().getSeq(), is(5L));
	}
	
	@Test
	@Order(5)	
	void should_remove_all() {
		
		repository.deleteAll();
		
		assertThat(repository.count(), is(0L));
	}

}
