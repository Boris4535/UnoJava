package org.openjfx.model;

import org.openjfx.model.Player;

import java.util.Map;

public class ScoreBasedGame implements GameMode{

    GameMode mode;
    public int targetPoints = 500;

    public ScoreBasedGame(){}

    public ScoreBasedGame(int maxScoreToReach){
        this.targetPoints = maxScoreToReach;
    }

    @Override
    public boolean isMatchOver(GameState state) {
        Map<Player,Integer> players = state.getMatchStats().getPointsPerPlayer();
        Player currentPlayer = state.getCurrentPlayer();

        return players.get(currentPlayer) >= targetPoints;

    }

}
