package org.openjfx.model;


import java.io.Serializable;
import java.util.*;

import static org.openjfx.model.Color.*;
import static org.openjfx.model.CardType.*;

/** GameState contains the current state of an ongoing match.
 * It remembers the players, their position and the direction of the match (clockwise or not).
 * It also initializes the deck, shuffles it, tracks its lifecycle with the discard pile (which can be turned into a new deck)
 * and manages any interaction with the deck.
 * The statistics of the match are registered by {@link MatchStats}
 * @author Leon Balbo
 * @author Lucia Annicchiarico
 * @author Andriy Chyzhevskyy
 */

public class GameState implements Serializable {
    public Stack<Card> drawPile = new Stack<>();
    public Stack<Card> discardPile = new Stack<>();
    public List<Player> players;
    public int currentPlayerIndex;
    public Color currentColor;
    public boolean clockwisePhase = true;
    public int pendingDrawPenalty = 0;
    public CardType activeStackType = null;
    public MatchSettings settings;
    private MatchStats matchStats; // tiene le statistiche di ogni singola partita

    /** sole constructor, sets up the state of the game ready to function and establishes the players
     * @param nPlayers list of players
     */
    public GameState(List<Player> nPlayers, MatchSettings settings) {
        this.settings = settings;
        initDrawPile();
        matchStats = new MatchStats(nPlayers, this.settings);
        players = nPlayers;
    }

    /** returns currentPlayer
     */
    public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

    /** inverts the clock the opposite way
     */
    public void invertClock(){
        clockwisePhase = false;
    }

    /** nextTurn() moves to the next player in the list, and takes into account the clockwisePhase
     */
    public void nextTurn(){
        int numPlayers = players.size();
        if(clockwisePhase)
            this.currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
        else
            this.currentPlayerIndex = (currentPlayerIndex - 1 + numPlayers) % numPlayers;
    }

    /** getNextPlayer() returns next player in the list WITHOUT moving the list.
     * To actually move the list, check nextTurn()
     * @return next player in line
     */
    public Player getNextPlayer(){
        int numPlayers = players.size();
        int indexNextPlayer = 0;
        if(clockwisePhase)
            indexNextPlayer = (currentPlayerIndex + 1) % numPlayers;
        else
            indexNextPlayer = (currentPlayerIndex - 1 + numPlayers) % numPlayers;
        return players.get(indexNextPlayer);
    }

    /** shuffles a stack of cards in a random pattern
     * @param cardStack deck or cards to shuffle
     * @return shuffled stack of cards
     */
    public Stack<Card> shuffle(Stack<Card> cardStack){
        List<Card> temp = new ArrayList<>();
        temp.addAll(cardStack);
        Collections.shuffle(temp);
        Stack<Card> temp2 = new Stack<>();
        temp2.addAll(temp);
        return temp2;
    }

    /** builds a pile of cards following the standard Uno-style card deck.
     *  Therefore, the deck contains:
     *  <ul>
     *  <li>76 Number cards (of all colors; one '0' and two of each from '1' to '9' per color).</li>
     *  <li>24 Action cards (two Skip, two Reverse, and two Draw Two per color).</li>
     *  <li>8 Wild cards.</li>
     *  </ul>
     *  The deck is then shuffled and ready to use.
     */
    public void initDrawPile(){

        for (Color currentColor : Color.values()) {
            if (currentColor != WILD) {
                for (CardType currentType : CardType.values()) {
                    if (currentType == NUMBERS) {
                        drawPile.add(new Card(currentColor, NUMBERS, 0));
                        for (int i = 1; i < 10; i++) {
                            drawPile.add(new Card(currentColor, NUMBERS, i));
                            drawPile.add(new Card(currentColor, NUMBERS, i));
                        }
                    } else if (currentType != WILD_JOLLY && currentType != WILD_DRAW) {
                        drawPile.add(new Card(currentColor, currentType));
                        drawPile.add(new Card(currentColor, currentType));
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    drawPile.add(new Card(currentColor, WILD_JOLLY));
                    drawPile.add(new Card(currentColor, WILD_DRAW));
                }
            }
        }

        Collections.shuffle(drawPile);
    }

    /** draws a specified amount of cards from the deck
     * @param nCards number of cards to draw
     * @return the stack of cards taken from the deck
     * @throws IndexOutOfBoundsException if nCards is greater than the size of the deck
     */
    public Stack<Card> getTopCards(int nCards){
        // DEPRECATED:if (nCards > drawPile.size()) throw new IndexOutOfBoundsException();
        //il for successivo gestisce tutto perfettamente, questo metodo causava crash continui in simulazione

        Stack<Card> temp = new Stack<Card>();
        for(int i = 0; i < nCards;i++){//FIX: In caso il mazzo sia vuoto
            if(drawPile.isEmpty()) reshuffleDiscardIntoDraw();
            temp.add(drawPile.pop());
        }
        return temp;
    }

    /**
     * @return matchStats
     */
    public MatchStats getMatchStats(){
        return this.matchStats;
    }

    /** shuffles the discardPile so it can be reused as draw pile
     */
    public void setMatchStats(MatchStats matchStats) {
        this.matchStats = matchStats;
    }

    public void reshuffleDiscardIntoDraw(){
        drawPile = shuffle(discardPile);
        discardPile.clear(); //Mancava ripulire la pila dopo rimescolato
    }
}