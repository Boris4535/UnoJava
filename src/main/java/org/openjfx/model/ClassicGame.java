package org.openjfx.model;

import org.openjfx.model.Player;

public class ClassicGame implements GameMode{

    private Player winner;

    @Override
    public int getWinnerScore(Player winner) { // come parametro il giocatore vincente
        // ritorno il suo score, yah
        return 0;
    }

    @Override
    public boolean isMatchOver(Player player) {
        return player.getHandSize() == 0;
    }
}
