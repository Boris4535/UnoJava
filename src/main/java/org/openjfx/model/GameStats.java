package org.openjfx.model;

/** Collects, aggregates, and calculates global statistics for
 * all matches ever played in the game.
 * This class tracks data such as total penalties, total challenges,
 * and calculated averages for scores, rounds and turns.
 * @author Lucia Annicchiarico
 */
public class GameStats {
    private int numOfRounds = 0; // avgNumOfRounds
    private int numNumOfTurns = 0; // avgNumOfTurns
    private int numOfChallenges = 0;
    private int numOfPenalties = 0;
    private int numOfPlayedMatches = 0;
    private int totalGlobalScore = 0;
    private static GameStats instance;

    private GameStats(){}

    public static GameStats getInstance() {
        if (instance == null) {
            instance = new GameStats();
        }
        return instance;
    }

    /** Extracts and accumulates statistics from a finished match into the global records.
     * @param nMatch match just completed
     */
    public void registerMatch(MatchStats nMatch){
        this.numOfPlayedMatches++;
        this.numOfRounds += nMatch.getNumOfRounds();
        this.numNumOfTurns += nMatch.getNumNumOfTurns();
        this.numOfPenalties += nMatch.getNumOfPenalties();
        this.numOfChallenges += nMatch.getNumOfChallenges();

        this.totalGlobalScore += nMatch.getPointsFromAllPlayers();
    }

    /** Calculates the average total score generated per match.
     * @return average score per match, or 0.0 if no matches have been played
     */
    public double avgScorePerMatch(){
        if (numOfPlayedMatches <= 0) return 0.0;
        return (double) totalGlobalScore / numOfPlayedMatches;
    }

    /** Calculates the average number of rounds played per match.
     * @return the average rounds per match, or 0.0 if no matches have been played
     */
    public double getAvgNumOfRounds(){
        if (numOfPlayedMatches <= 0) return 0.0;
        return (numOfRounds == 0) ? 0.0 : (double) numOfRounds / numOfPlayedMatches;
    }

    /** Calculates the average number of turns taken per single round.
     *
     * @return the average turns per round, or 0.0 if no rounds have been played
     */
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
