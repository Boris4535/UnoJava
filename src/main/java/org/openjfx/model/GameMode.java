package org.openjfx.model;

import org.openjfx.model.Card;
import org.openjfx.model.Player;

import java.util.List;
/** defines the strategy for determining when a match is officially over.
 * Implementations of this interface define different winning conditions.
 * Currently, two game modes are implemented: {@link ClassicGame} and {@link ScoreBasedGame}
 * @author Lucia Annicchiarico
 */
public interface GameMode {

    /** Checks whether the current match has met the end game criteria
     * @param state the current game state to evaluate
     * @return true if the criteria has been met, false otherwise
     */
    boolean isMatchOver(GameState state);

}
