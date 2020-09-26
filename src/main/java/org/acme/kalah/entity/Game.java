package org.acme.kalah.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.acme.kalah.error.InvalidMoveException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "game")
public class Game {
	
	
	@Transient
    public static final String SEQUENCE_NAME = "game_sequence";
	
	@Transient
    public static final int PITS_NUMBER = 14;
	
	public Game() {
		
		Integer[] array=new Integer[PITS_NUMBER];
		Arrays.fill(array, 6);
		
		this.pits=Arrays.asList(array);
		this.turn=true;
		
		
		array=null;
	}
	
	@Id
	private Long id;

	@Size(min = PITS_NUMBER,max = PITS_NUMBER,message = "number of pits is "+PITS_NUMBER)
	private List<Integer> pits;

	
	private Boolean turn;

	@PositiveOrZero
	private int kala1;

	@PositiveOrZero
	private int kala2;

	private Integer winner=-1;

	public int getKala2() {
		return kala2;
	}

	public void setKala2(int kala2) {
		this.kala2 = kala2;
	}

	public int getKala1() {
		return kala1;
	}

	public void setKala1(int kala1) {
		this.kala1 = kala1;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getTurn() {
		return turn;
	}

	public void setTurn(Boolean turn) {
		this.turn = turn;
	}

	public void incrementKala1(int i) {
		kala1=kala1+i;
		
	}

	public void incrementKala2(int i) {
		kala2=kala2+i;
		
	}

	public void setPits(java.util.List<Integer> pits) {
		this.pits = pits;
	}
	public List<Integer> getPits() {
		
		return pits;
	}

	public void validateMove(Integer pitId) throws InvalidMoveException{
		if (Boolean.TRUE.equals(turn) && (pitId > PITS_NUMBER/2)) {
			throw new InvalidMoveException("Is player 1 turn but you are trying to move pits of player 2");
		}
		if (Boolean.FALSE.equals(turn) && (pitId <(PITS_NUMBER/2)+1)) {
			throw new InvalidMoveException("Is player 2 turn but you are trying to move pits of player 1");
		}
		int tokens=pits.get(pitId-1);
		if (tokens==0) {
			throw new InvalidMoveException("You are trying to move on an empty pit");
		}
	}

	public int getFrontPitIndex(int index) {
				
		return pits.size()-1-index;
	}

	/**
	 * stealing rule is applicable only when the last token is not in the opposite player row
	 * and the front pit contains tokens
	 * @param index
	 * @return
	 */
	public boolean stealingRuleIsApplicable(int index) {
		int frontPitIndex= getFrontPitIndex(index);
		
		if (pits.get(frontPitIndex)==0) {
			return false;
		}
		
		return ((Boolean.TRUE.equals(turn) && frontPitIndex > (PITS_NUMBER/2)-1) || (Boolean.FALSE.equals(turn) && frontPitIndex<PITS_NUMBER/2));
	}

	public boolean isFinished() {
		List<Integer> tempList=new ArrayList<>(pits);
		
		boolean pits1ContainsToken=false;
		boolean pits2ContainsToken=false;
		
		// check if first pit row contains some token
		Optional<Integer> remainingTokens1=pits
				.parallelStream()
				.limit(PITS_NUMBER/2).reduce((a,b)->a+b);
		
		if (remainingTokens1.isPresent() && remainingTokens1.get()>0) {
			pits1ContainsToken=true;
		}
		// check if next pit row contains some token
		Collections.reverse(pits);	
		Optional<Integer> remainingTokens2=pits
				.parallelStream()
				.limit(PITS_NUMBER/2).reduce((a,b)->a+b);
		
		if (remainingTokens2.isPresent() && remainingTokens2.get()>0) {
			pits2ContainsToken=true;
		}
		
		pits=new ArrayList<>(tempList);
				
		// game is finished when one of the player set of pits are empty
		return !pits1ContainsToken || !pits2ContainsToken;
	}

	public void assignRemainingTokens() {
		
		Optional<Integer> remainingTokens=pits
				.parallelStream()
				.limit(PITS_NUMBER/2).reduce((a,b)->a+b);
		
		if (remainingTokens.isPresent() && remainingTokens.get()>0) {
			// assign remaining tokens to player 1 Kalah
			incrementKala1(remainingTokens.get());
			
		}else {
			// at this point if player 1 has no remaining tokens,
			// then remaining tokens are in the second set of pits
			Collections.reverse(pits);	
			remainingTokens=pits
					.parallelStream()
					.limit(PITS_NUMBER/2).reduce((a,b)->a+b);
			if (remainingTokens.isPresent() && remainingTokens.get()>0) {
				incrementKala2(remainingTokens.get());
			}	
		}
		// finally empty all pits
		pits=pits
				.parallelStream()
				.map(token->0).collect(Collectors.toList());
	}

	public Integer assignWinner() {
		if (getKala1()>getKala2()) {
			return 1;
		}
		if (getKala2()>getKala1()) {
			return 2;
		}
		return 0;
	}

	public void setWinner(Integer assignedWinner) {
		this.winner=assignedWinner;
	}

	public Integer getWinner() {
		return winner;
	}
	
}
