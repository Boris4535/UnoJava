package org.openjfx;


import java.util.*;

import org.openjfx.Color;
import org.openjfx.CardType;

import static org.openjfx.Color.*;
import static org.openjfx.CardType.*;


public class GameState {
    public Stack<Card> drawPile = new Stack<>();
    public Stack<Card> discardPile = new Stack<>();
    public List<Player> players;
    public int currentPlayerIndex;
    public Color currentColor;
    public boolean clockwisePhase = true;

    private Stack<Card> shuffle(Stack<Card> cardStack){
        List<Card> temp = new ArrayList<>();
        temp.addAll(cardStack);
        Collections.shuffle(temp);
        Stack<Card> temp2 = new Stack<>();
        temp2.addAll(temp);
        return temp2;
    }

    private void initDrawPile(){
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
                    else if(types.next() != WILD_JOLLY && types.next() != WILD_DRAW){
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

    public void reshuffleDiscardIntoDraw(){ drawPile = shuffle(discardPile);}
}
