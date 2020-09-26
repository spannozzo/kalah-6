package org.acme.kalah.model;

public class KalahMoveModel {
	
	private Integer index; 
	private Integer tokensLeft; 
	private Boolean nextTurn;
	private Integer currentPitTokens;
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public Integer getTokensLeft() {
		return tokensLeft;
	}
	public void setTokensLeft(Integer tokensLeft) {
		this.tokensLeft = tokensLeft;
	}
	public Boolean getNextTurn() {
		return nextTurn;
	}
	public void setNextTurn(Boolean nextTurn) {
		this.nextTurn = nextTurn;
	}
	
	public KalahMoveModel(Integer index, Integer tokensLeft, Boolean nextTurn) {
		super();
		this.index = index;
		this.tokensLeft = tokensLeft;
		this.nextTurn = nextTurn;
		
		this.currentPitTokens=0;
		
	}
	public boolean canMove() {
		return this.tokensLeft > 0;
	}
	public void setCurrentPitTokens(Integer currentPitTokens) {
		this.currentPitTokens=currentPitTokens;
	}
	public Integer getCurrentPitTokens() {
		return currentPitTokens;
	}
	public void decreaseTokens() {
		this.tokensLeft--;			
	}
	public void nextPit() {
		index++;
	}
	public void fromBegin() {
		index=-1;
	}
	public void playAgain() {
		nextTurn=!nextTurn;
	}
	
}
