package org.openjfx.model;

import org.openjfx.model.Card;
import org.openjfx.model.Player;

/** Imposes the fundamental structure of the game onto the (current) two gamemods (single-player,
 * score-based), such as keeping score of scores
 */
public interface GameMode {

    int getWinnerScore(Player winner);

    boolean isMatchOver(Player player);

    public default int calculateScores(Player player){
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
}
