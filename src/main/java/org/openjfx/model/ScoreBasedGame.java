package org.openjfx.model;

import org.openjfx.model.Player;

public class ScoreBasedGame implements GameMode{

    GameMode mode;

    @Override
    public boolean isMatchOver(GameState state) {
        for (Player player : state.players) {
            if (player.getMatchScore() >= 500) {
                return true;
            }
        }
        return false;
    }



}
