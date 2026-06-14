package org.openjfx.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openjfx.model.CardType.SKIP;

public class MatchStats {

    private int numOfRounds = 0;
    private int numNumOfTurns = 0;
    private Map<Player,Integer> pointsPerPlayer;
    private Map<Player,Integer> penaltiesPerPlayer;
    private Map<Player,Integer> challengesPerPlayer;

    public MatchStats(List<Player> players){
        pointsPerPlayer = new HashMap<>();
        penaltiesPerPlayer = new HashMap<>();
        challengesPerPlayer = new HashMap<>();

        for(Player p : players){
            pointsPerPlayer.put(p,0);
            penaltiesPerPlayer.put(p,0);
            challengesPerPlayer.put(p,0);
        }
    }


    public void incrementRounds(){
        this.numOfRounds++;
    }

    public void incrementTurns(){
        this.numNumOfTurns++;
    }

    public int extractPointsFromCard(Card card){
        int score = 0;
        switch (card.getType()) {
            case SKIP, REVERSE, DRAW_TWO
                    -> score += 20;
            case WILD_DRAW, WILD_JOLLY
                    -> score += 50;
            default -> // number card
                    score += card.getValue();
        }
        return score;
    }

    public int getPointsFromAllPlayers(){
        return pointsPerPlayer  .values()
                                .stream()
                                .reduce(0, Integer::sum);
    }

    public int getNumOfPenalties(){
        return penaltiesPerPlayer.values().stream().reduce(0,Integer::sum);
    }

    public int getNumOfChallenges(){
        return challengesPerPlayer.values().stream().reduce(0,Integer::sum);
    }

    public int getNumOfRounds(){
        return this.numOfRounds;
    }

    public int getNumNumOfTurns() {
        return numNumOfTurns;
    }

    public void incrementPointsTo(Player player, Card card){
        int pointsToAdd = extractPointsFromCard(card);
        pointsPerPlayer.put(player, pointsPerPlayer.get(player) + pointsToAdd);
    }

    public void decrementPointsTo(Player player, Card card){
        int pointsToSubtract = extractPointsFromCard(card);
        pointsPerPlayer.put(player,pointsPerPlayer.get(player) - pointsToSubtract);
    }

    public void incrementPenalties(Player player){
        penaltiesPerPlayer.put(player, penaltiesPerPlayer.get(player) + 1);
    }

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
