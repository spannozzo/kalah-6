package org.acme.kalah.service;

import org.acme.kalah.entity.Game;
import org.acme.kalah.model.KalahMoveModel;

public interface KalahModelService {

	
	void updateBoard(Game game, KalahMoveModel kalahMoveStatus);

}
