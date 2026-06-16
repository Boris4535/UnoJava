package org.openjfx.model;

import java.io.Serializable;

/*
 *Le statistiche devono includere almeno:
numero di partite vinte da ciascun giocatore o profilo di bot;
punteggio medio;
numero medio di round per partita;
numero medio di turni per round;
numero totale di challenge;
numero totale di penalità applicate.
 *
/** keeps track of a player lifetime statistics, specifically:
 * <li>Number of won matches</li>
 * <li>Score accumulated from all matches</li>
 * <li>Number of penalties accumulated</li>
 * <li>Number of challenges accumulated</li>
 * @author Lucia Annicchiarico
 */
public class PlayerStats implements Serializable {
    private int wonMatches = 0;
    private int overallScore = 0;
    private int totalMatches = 0;
    private int numPenalties = 0;
    private int numChallenges = 0;

    /** Updates the player's lifetime statistics with the results of a completed match
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

    /** Adds the points from a newly completed match to the player's total lifetime score
     */
    public void updateScore(int nScore){
        if (nScore > 0) this.overallScore += nScore;
    }

    public int getTotalWonMatches(){
        return wonMatches;
    }

}
