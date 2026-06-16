package org.openjfx.model;

import org.openjfx.model.Player;

/** Implements the classic win condition in a match.<br>
 * In this mode, only one match is played, which ends once one player
 * has discarded all cards from their hand.
 * @author Lucia Annicchiarico
 */
public class ClassicGame implements GameMode{

    /** Determines if the match is over by verifying if the current player
     * has an empty hand.
     * @param state the current game state to evaluate
     * @return true if the hand is empty, false otherwise
     */
    @Override
    public boolean isMatchOver(GameState state) {
        return state.getCurrentPlayer().getHandSize() == 0;
    }
}
