package org.acme.kalah.service;

import org.acme.kalah.dto.MoveDTO;
import org.acme.kalah.entity.Game;
import org.acme.kalah.model.KalahMoveModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoveServiceImpl implements MoveService {


	@Autowired
	private GameService gameService;
	
	@Autowired
	private KalahModelService kalahModelService;

	public MoveDTO makeMove(Game game, Integer pitId) {
		
		// before moving, check if there is already a winner
		if(game.getWinner()<0) {
			this.move(game, pitId);
			gameService.save(game);
		}	
		
		return gameService.getMoveDTO(game);
	}

	private void move(Game game, Integer pitId) {
	 // store temp. tokens left
		Integer tokensLeft = game.getPits().get(pitId-1);
		
	 // remove tokens from original pit
		game.getPits().set(pitId-1, 0);

     //	start pit token assignment from next pit
		Integer index = pitId ;

	//	recording next turn (by default is opposite turn)
		Boolean nextTurn=!game.getTurn();
		
		// store initial values into a board move status model
		KalahMoveModel kalahMoveStatus=new KalahMoveModel(index, tokensLeft, nextTurn);
		
//		delegate this service in order to update pits, scores, and apply game rules
		kalahModelService.updateBoard(game,kalahMoveStatus);
		
		// define next turn
		game.setTurn(kalahMoveStatus.getNextTurn());
		
		if(game.isFinished()) {
			game.assignRemainingTokens();
			game.setWinner(game.assignWinner());
		}
		
		
	}
	
	

	
}
