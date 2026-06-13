package org.openjfx.model;

public class GameStats {
    private int numOfRounds = 0; // avgNumOfRounds
    private int numNumOfTurns = 0; // avgNumOfTurns
    private int numOfChallenges = 0;
    private int numOfPenalties = 0;
    private int numOfPlayedMatches = 0;
    private int totalGlocalScore = 0;

    public void registerMatch(MatchStats nMatch){
        this.numOfPlayedMatches++;
        this.numOfRounds += nMatch.getNumOfRounds();
        this.numNumOfTurns += nMatch.getNumNumOfTurns();
        this.numOfPenalties += nMatch.getNumOfPenalties();
        this.numOfChallenges += nMatch.getNumOfChallenges();

        this.totalGlocalScore += nMatch.getPointsFromAllPlayers();
    }

    public double avgScorePerMatch(){
        if (numOfPlayedMatches <= 0) return 0.0;
        return (double) totalGlocalScore / numOfPlayedMatches;
    }

    public double getAvgNumOfRounds(){
        if (numOfPlayedMatches <= 0) return 0.0;
        return (numOfRounds == 0) ? 0.0 : (double) numOfRounds / numOfPlayedMatches;
    }

    public double getAvgNumOfTurns(){
        if (numOfRounds <= 0) return 0.0; // evitiamo di dividere per 0
        return (numNumOfTurns == 0) ? 0.0 : (double) numNumOfTurns / numOfRounds;
    }

    public int getNumOfChallenges() {
        return numOfChallenges;
    }

    public int getNumOfPenalties() {
        return numOfPenalties;
    }

    public int getNumOfPlayedMatches() {
        return numOfPlayedMatches;
    }
}
