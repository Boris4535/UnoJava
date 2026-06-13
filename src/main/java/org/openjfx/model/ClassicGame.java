package org.openjfx.model;

import org.openjfx.model.Player;

public class ClassicGame implements GameMode{

    @Override
    public boolean isMatchOver(GameState state) {
        return state.getCurrentPlayer().getHandSize() == 0;
    }
}
