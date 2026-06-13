package org.openjfx.model;

import org.openjfx.model.Card;
import org.openjfx.model.Player;

import java.util.List;

/** Imposes the fundamental structure of the game onto the (current) two gamemods (single-player,
 * score-based), such as keeping score of scores
 */
public interface GameMode {

    boolean isMatchOver(GameState state);

}
