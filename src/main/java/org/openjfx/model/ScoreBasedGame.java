package org.openjfx.model;

import org.openjfx.model.Player;

public class ScoreBasedGame implements GameMode{

    @Override
    public int getWinnerScore(Player winner) {
        return 0;
    }

    @Override
    public boolean isMatchOver(Player player) {
        return player.getIntPoints() >= 500;
    }


}
