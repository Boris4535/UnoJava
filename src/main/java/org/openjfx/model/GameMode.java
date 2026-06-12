package org.openjfx.model;

import org.openjfx.model.Card;
import org.openjfx.model.Player;

import java.util.List;

/** Imposes the fundamental structure of the game onto the (current) two gamemods (single-player,
 * score-based), such as keeping score of scores
 */
public interface GameMode {

    boolean isMatchOver(GameState state);

    public default int calculateCardScore(Player player){
        int score = 0;
        for(Card card : player.getHand()){
            switch (card.getType()){
                case SKIP, REVERSE, DRAW_TWO ->
                    score += 20;
                case WILD_DRAW, WILD_JOLLY ->
                    score += 50;
                default -> // number card
                    score += card.getValue();
            }
        }
        return score;
    }

    public default int calculateOverallScore(Player winner, List<Player> others){
        int score = -1;
        score = others  .stream()
                            .filter(player -> !player.equals(winner)) // da controllare
                            .mapToInt(other -> calculateCardScore(other))
                            .sum();
        return score;
    }
}
