package org.openjfx.model;

import java.util.List;

/**
 * Defines a single card and its properties, and a method to check if it is playable
 * @author leon445
 */
public class Card {
    private Color color;
    private CardType type;
    private int value = -1;

    public Card(Color color, CardType type, int value){
        this.color = color;
        this.type = type;
        this.value = value;
    }
    public Card(Color color, CardType type){
        this.color = color;
        this.type = type;
    }

    public Color getColor(){return this.color;}
    public int getValue(){return this.value;}
    public CardType getType(){return this.type;}


    /**
     * Checks if the card is playable on top of the card passed by the parameter, usually
     * card atop the discard pile.
     * @param topCard the card that is being checked with
     * @return isPlayable
     */
    public boolean isPlayableOn(Card topCard){
        //Carte wild
        if (this.color == Color.WILD) {
            return true;
        }

        //Colori, controllo sia per i numeri che per le speciali
        if (this.color == topCard.getColor()) {
            return true;
        }
        //numeriche
        if (this.type == CardType.NUMBERS && topCard.getType() == CardType.NUMBERS) {
            return this.value == topCard.getValue();
        }

        //ILLEGALI
        return false;

    }

}
