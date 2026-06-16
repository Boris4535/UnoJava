package org.openjfx.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.openjfx.model.CardType.SKIP;

/** Tracks and manages the statistics and scores of an active match.
 * It keeps tracks of the number of rounds (matches) and turns.
 * Player specific statistics are also kept, such as challenges, penalties accumulated
 * by each player
 * This class serves as the foundation for all game statistics. At the end of a match,
 * this data is processed to update individual records in {@link PlayerStats}
 * and global history inside {@link GameStats}.
 * @author Lucia Annicchiarico
 */
public class MatchStats implements Serializable {

    private int numOfRounds = 0;
    private int numNumOfTurns = 0;
    private Map<Player,Integer> pointsPerPlayer;
    private Map<Player,Integer> penaltiesPerPlayer;
    private Map<Player,Integer> challengesPerPlayer;
    private List<Move> moveHistory = new ArrayList<>();
    private boolean isCustomScoring = false;
    private MatchSettings settings;

    public void setCustomScoring(boolean customScoring) {
        this.isCustomScoring = customScoring;
    }

    /**Constructs a new MatchStats instance,
     * initializing maps to keep track of each player and their points
     * @param players list of players whose records will be created
     */
    public MatchStats(List<Player> players, MatchSettings settings){
        this.settings = settings;
        this.isCustomScoring = settings.customScoringEnabled;
        pointsPerPlayer = new HashMap<>();
        penaltiesPerPlayer = new HashMap<>();
        challengesPerPlayer = new HashMap<>();

        for(Player p : players){
            pointsPerPlayer.put(p,0);
            penaltiesPerPlayer.put(p,0);
            challengesPerPlayer.put(p,0);
        }

    }
    /** Converts a card's game property into its scoring value.
     * Currently, special cards (Skip, Reverse, Draw Two) are valued at 20 points, Wild cards
     * at 50 points, and number cards carry their written value.
     * @param card to examine
     * @return value of the given card
     */
    public int extractPointsFromCard(Card card){  //Refactored funzione di lucia
        int score = 0;
        if (isCustomScoring) {
            switch (card.getType()) {
                case SKIP, REVERSE, DRAW_TWO -> score += 10;
                case WILD_DRAW, WILD_JOLLY -> score += 25;
                default -> score += settings.customNumberValue; // Regola custom: i numeri valgono 5
            }
        } else {
            switch (card.getType()) {
                case SKIP, REVERSE, DRAW_TWO -> score += 20;
                case WILD_DRAW, WILD_JOLLY -> score += 50;
                default -> score += card.getValue();
            }
        }
        return score;
    }


    public void addMove(String playerName, String action) {
        moveHistory.add(new Move(playerName, action));
    }
    public List<Move> getMoveHistory() {
        return moveHistory;
    }



    /**
     * Increments the total round counter.
     */
    public void incrementRounds(){
        this.numOfRounds++;
    }

    /**
     * Increments the total turns counter.
     */
    public void incrementTurns(){
        this.numNumOfTurns++;
    }


        /** Calculates the sum of all points accumulated by all players combined.
     * @return number of accumulated points
     */
    public int getPointsFromAllPlayers(){
        return pointsPerPlayer  .values()
                                .stream()
                                .reduce(0, Integer::sum);
    }

    /** Calculates the total number of penalties raised by players
     * @return total number of penalties
     */
    public int getNumOfPenalties(){
        return penaltiesPerPlayer.values().stream().reduce(0,Integer::sum);
    }

    /**Calculates the total number of challenges evoked by players.
     * @return the total of challenges
     */
    public int getNumOfChallenges(){
        return challengesPerPlayer.values().stream().reduce(0,Integer::sum);
    }

    public int getNumOfRounds(){
        return this.numOfRounds;
    }

    public int getNumNumOfTurns() {
        return numNumOfTurns;
    }

    /** Examines a card and adds its point value to a specific player's score.
     * This happens when a player draws a card.
     * @param player
     * @param card to evaluate
     */
    public void incrementPointsTo(Player player, Card card){
        int pointsToAdd = extractPointsFromCard(card);
        pointsPerPlayer.put(player, pointsPerPlayer.get(player) + pointsToAdd);
    }

    /** Evaluates a card and detract its point value to a specific player's score.
     * This happens when a player plays a card.
     * @param player
     * @param card to examine
     */
    public void decrementPointsTo(Player player, Card card){
        int pointsToSubtract = extractPointsFromCard(card);
        pointsPerPlayer.put(player,pointsPerPlayer.get(player) - pointsToSubtract);
    }

    /** Increments the penalty count for a specific player by 1.
     * @param player
     */
    public void incrementPenalties(Player player){
        penaltiesPerPlayer.put(player, penaltiesPerPlayer.get(player) + 1);
    }

    /** Increments the challenges count for a specific player by 1.
     * @param player
     */
    public void incrementChallenges(Player player){
        challengesPerPlayer.put(player, challengesPerPlayer.get(player) + 1);
    }

    public Map<Player, Integer> getPointsPerPlayer() {
        return pointsPerPlayer;
    }

    public Map<Player, Integer> getPenaltiesPerPlayer() {
        return penaltiesPerPlayer;
    }

    public Map<Player, Integer> getChallengesPerPlayer() {
        return challengesPerPlayer;
    }


}
