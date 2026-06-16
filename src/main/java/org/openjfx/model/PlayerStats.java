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
 */
public class PlayerStats implements Serializable {
    private int wonMatches = 0;
    private int overallScore = 0;
    private int totalMatches = 0;
    private int numPenalties = 0;
    private int numChallenges = 0;

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

    public void updateScore(int nScore){
        if (nScore > 0) this.overallScore += nScore;
    }

    public int getTotalWonMatches(){
        return wonMatches;
    }

}
