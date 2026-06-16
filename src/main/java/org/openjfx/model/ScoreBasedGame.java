package org.openjfx.model;

import org.openjfx.model.Player;

import java.util.Map;

/** Implements a score based win condition for a match.<br>
 * In this mode, multiple matches can potentially be played,
 * until at least one player reaches the target score.
 * @author Lucia Annicchiarico
 */
public class ScoreBasedGame implements GameMode{

    public int targetPoints = 500;

    public ScoreBasedGame(){}

    /** Determines if the match is over based on whether the current round winner
     * has reached (or exceeded) the target score.
     * @param state the current game state to evaluate
     * @return true if at least one player has reached the target score, false otherwise
     */
    @Override
    public boolean isMatchOver(GameState state) {
        Map<Player,Integer> players = state.getMatchStats().getPointsPerPlayer();
        Player currentPlayer = state.getCurrentPlayer();

        return players.get(currentPlayer) >= targetPoints;
    }

    public void setTargetPoints(int nTargetPoints) {
        this.targetPoints = nTargetPoints;
    }
}
