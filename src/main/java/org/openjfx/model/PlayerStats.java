package org.openjfx.model;

import java.io.Serializable;

/** Keeps track of a player lifetime statistics, specifically:
 * - Number of won matches<br>
 * - Score accumulated from all matches<br>
 * - Number of penalties accumulated<br>
 * - Number of challenges accumulated<br>
 * @author Lucia Annicchiarico
 */
public class PlayerStats implements Serializable {
    private int wonMatches = 0;
    private int overallScore = 0;
    private int totalMatches = 0;
    private int numPenalties = 0;
    private int numChallenges = 0;

    /** Updates the player's lifetime statistics with the results of a completed match.
     * @param hasWon whether the player has won the match
     * @param winningScore if the match has been won, the points earned
     * @param numPenalties number of penalties accumulated by the player
     * @param numChallenges number of challenges accumulated by the player
     */
    public void registerMatch(boolean hasWon, int winningScore, int numPenalties, int numChallenges){
        this.totalMatches++;
        this.numPenalties += numPenalties;
        this.numChallenges += numChallenges;

        if (hasWon) {
            wonMatches++;
            updateScore(winningScore);
        }

    }

    public int getOverallScore() {
        return this.overallScore;
    }
    public void setOverallScore(int nMatchScore){
        this.overallScore = nMatchScore;
    }

    /** Adds the points from a newly completed match to the player's total lifetime score.
     */
    public void updateScore(int nScore){
        if (nScore > 0) this.overallScore += nScore;
    }

    public int getTotalWonMatches(){
        return wonMatches;
    }

}
