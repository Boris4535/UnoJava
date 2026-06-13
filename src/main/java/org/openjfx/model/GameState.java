package org.openjfx.model;


import java.util.*;

import static org.openjfx.model.Color.*;
import static org.openjfx.model.CardType.*;


public class GameState {
    public Stack<Card> drawPile = new Stack<>();
    public Stack<Card> discardPile = new Stack<>();
    public List<Player> players;
    public int currentPlayerIndex;
    public Color currentColor;
    public boolean clockwisePhase = true;
    private MatchStats matchStats; // tiene le statistiche di ogni singola partita

    public GameState() {
        initDrawPile();
    }

    /** returns currentPlayer
     */
    public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

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
     * @return next player
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

    public Stack<Card> shuffle(Stack<Card> cardStack){
        List<Card> temp = new ArrayList<>();
        temp.addAll(cardStack);
        Collections.shuffle(temp);
        Stack<Card> temp2 = new Stack<>();
        temp2.addAll(temp);
        return temp2;
    }

    public void initDrawPile(){
        Iterator<Color> colors = Arrays.asList(Color.values()).iterator();

        while(colors.hasNext()){
            Color currentColor = colors.next();
            if(currentColor != WILD){
                //Iteratore per ogni colore, così da non saltarli
                Iterator<CardType> types = Arrays.asList(CardType.values()).iterator();
                while(types.hasNext()) {
                    CardType currentType = types.next();
                        if (currentType == NUMBERS) {
                        drawPile.add(new Card(currentColor, NUMBERS, 0));
                        for (int i = 1; i < 10; i++) {
                            drawPile.add(new Card(currentColor, NUMBERS, i));
                            drawPile.add(new Card(currentColor, NUMBERS, i));
                        }
                    }
                    else if(currentType != WILD_JOLLY && currentType != WILD_DRAW){
                        //Piccolo fix qui, simile a quello dell'iteratore precedente
                        drawPile.add(new Card(currentColor, currentType));
                        drawPile.add(new Card(currentColor, currentType));
                    }
                }
            }
            else{
                for(int i = 0; i < 4; i++){
                    drawPile.add(new Card(currentColor, WILD_JOLLY));
                    drawPile.add(new Card(currentColor, WILD_DRAW));
                }
            }
        }

        Collections.shuffle(drawPile);
    }

    public Stack<Card> getTopCards(int cards){
        Stack<Card> temp = new Stack<Card>();
        for(int i = 0; i < cards;i++){
        temp.add(drawPile.pop());
        }
        return temp;
    }

    public MatchStats getMatchStats(){
        return this.getMatchStats();
    }

    public void reshuffleDiscardIntoDraw(){ drawPile = shuffle(discardPile);}
}
