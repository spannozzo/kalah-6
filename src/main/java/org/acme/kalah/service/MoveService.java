package org.acme.kalah.service;

import org.acme.kalah.dto.MoveDTO;
import org.acme.kalah.entity.Game;

public interface MoveService {

	public MoveDTO makeMove(Game game,Integer pitId);

}
