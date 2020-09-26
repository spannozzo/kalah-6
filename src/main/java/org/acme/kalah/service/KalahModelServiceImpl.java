package org.acme.kalah.service;

import org.acme.kalah.entity.Game;
import org.acme.kalah.model.KalahMoveModel;
import org.springframework.stereotype.Service;

@Service
public class KalahModelServiceImpl implements KalahModelService {

	@Override
	public void updateBoard(Game game, KalahMoveModel kalahMoveStatus) {
		manageLastPitMove(game, kalahMoveStatus);

		manageFirstPitMove(game, kalahMoveStatus);

		while (kalahMoveStatus.canMove()) {

			depositTokenIntoPit(game, kalahMoveStatus);

			checkTokensStealRule(game, kalahMoveStatus);

			kalahMoveStatus.decreaseTokens();

			manageEndOfPits1(game, kalahMoveStatus);

			manageEndOfPits2(game, kalahMoveStatus);

			// go to next pit
			kalahMoveStatus.nextPit();

		}

	}

	private void updateKalaStatusCurrentPitTokens(Game game, KalahMoveModel kalahMoveStatus) {
		kalahMoveStatus.setCurrentPitTokens(game.getPits().get(kalahMoveStatus.getIndex()));
	}

	private void depositTokenIntoPit(Game game, KalahMoveModel kalahMoveStatus) {
		updateKalaStatusCurrentPitTokens(game, kalahMoveStatus);
		game.getPits().set(kalahMoveStatus.getIndex(), kalahMoveStatus.getCurrentPitTokens() + 1);
	}

	private void checkTokensStealRule(Game game, KalahMoveModel kalahMoveModel) {
		if (kalahMoveModel.getTokensLeft() == 1 && kalahMoveModel.getCurrentPitTokens() == 0
				&& game.stealingRuleIsApplicable(kalahMoveModel.getIndex())) {
			// steel corresponding front pits
			int frontPitIndex = game.getFrontPitIndex(kalahMoveModel.getIndex());

			int steeledToken = game.getPits().get(frontPitIndex);
			game.getPits().set(frontPitIndex, 0);

			// set player pit at 0
			game.getPits().set(kalahMoveModel.getIndex(), 0);

			// add stolen tokens+1 into player Kalah

			if (Boolean.FALSE.equals(kalahMoveModel.getNextTurn())) {
				// player 1 turn
				game.incrementKala1(steeledToken + 1);
			} else {
				game.incrementKala2(steeledToken + 1);
			}
		}

	}

	private void manageEndOfPits2(Game game, KalahMoveModel kalahMoveModel) {
		if (kalahMoveModel.getIndex() == Game.PITS_NUMBER - 1) {
//			restart pit counting from begin
			kalahMoveModel.fromBegin();
			if (game.getTurn().equals(false) && kalahMoveModel.canMove()) {
			
			//  second Kalah reached on player 2 turn: put 1 token into it
				
				kalahMoveModel.setTokensLeft(manageKala2(game, kalahMoveModel.getTokensLeft()));
				
				kalahMoveModel.setNextTurn(checkTurn(kalahMoveModel));

			}

		}

	}

	/**
	 * if can't move player will do another turn
	 * @param kalahMoveModel
	 * @return
	 */
	private Boolean checkTurn(KalahMoveModel kalahMoveModel) {
		
		if (!kalahMoveModel.canMove()) {
			return !kalahMoveModel.getNextTurn();
		}

		return kalahMoveModel.getNextTurn();
	}

	/**
	 * first Kalah reached on player 1 turn: put 1 token into it
	 * 
	 * @param game
	 * @param kalahMoveModel
	 */
	private void manageEndOfPits1(Game game, KalahMoveModel kalahMoveModel) {
		if (kalahMoveModel.getIndex() == (Game.PITS_NUMBER / 2) - 1 && game.getTurn().equals(true)
				&& kalahMoveModel.canMove()) {
			
			kalahMoveModel.setTokensLeft(manageKala1(game, kalahMoveModel.getTokensLeft()));
			
			kalahMoveModel.setNextTurn(checkTurn(kalahMoveModel));
		}

	}

	/**
	 * last pit token check, add 1 token to player 1 Kalah, check if play again rule
	 * 
	 * @param game
	 * @param kalahMoveStatus
	 */
	private void manageFirstPitMove(Game game, KalahMoveModel kalahMoveModel) {
//		
		if (kalahMoveModel.getIndex() == game.getPits().size() / 2) {

			// add 1 token to kalah and decrease remaining token too
			game.incrementKala1(1);
			kalahMoveModel.decreaseTokens();

//			if are 0 tokens left, player 1 will play again
			checkPlayAgainRule(kalahMoveModel);
		}

	}

	/**
	 * last pit token check, add 1 token to player 1 Kalah, check if play again rule, 
	 * start from first pit
	 * 
	 * @param game
	 * @param kalahMoveModel
	 */
	private void manageLastPitMove(Game game, KalahMoveModel kalahMoveModel) {

		if (kalahMoveModel.getIndex() == game.getPits().size()) {
			// restart index
			kalahMoveModel.setIndex(0);

			// add 1 token to Kalah and decrease remaining token too
			game.incrementKala2(1);
			kalahMoveModel.decreaseTokens();

			checkPlayAgainRule(kalahMoveModel);

		}
	}

	/**
	 * if left tokens is 0 it will negate the turn boolean variable that have been
	 * inverted at begin
	 * 
	 * @param kalahMoveModel
	 */
	private void checkPlayAgainRule(KalahMoveModel kalahMoveModel) {
		if (!kalahMoveModel.canMove()) {
			kalahMoveModel.playAgain();
		}
	}

	private Integer manageKala2(Game game, Integer tokensLeft) {
		game.incrementKala2(1);
		tokensLeft--;

		return tokensLeft;
	}

	private Integer manageKala1(Game game, Integer tokensLeft) {

		game.incrementKala1(1);
		tokensLeft--;

		return tokensLeft;
	}

}
