package org.openjfx.model;

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

    public Color getcolor(){return this.color;}
    public int getValue(){return this.value;}
    public CardType getType(){return this.type;}

    public boolean isPlayableOn(Card topCard){
        return this.color == topCard.color ||
                this.color == Color.WILD ||
                this.value == topCard.value ||
                this.type == topCard.type;
    }

}
