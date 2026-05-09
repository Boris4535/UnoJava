package org.openjfx;

public class Card {
    Color color;
    CardType type;
    int value;

    public Card(Color color, CardType type, int value){
        this.color = color;
        this.type = type;
        this.value = value;
    }
    public Card(Color color, CardType type){
        this.color = color;
        this.type = type;
    }

    Color getcolor(){return this.color;}

    CardType getType(){return this.type;}

    boolean isPlayableOn(Card topCard){
        return this.color == topCard.color ||
                this.color == Color.WILD ||
                this.value == topCard.value ||
                this.type == topCard.type;
    }

}
