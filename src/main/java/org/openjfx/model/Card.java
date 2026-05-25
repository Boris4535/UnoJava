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



    /* momento debugging
    * il metodo prcedente, era quasi giusto, l'unica nueance è che il metodo controlla sempre se alla fine
    * il type è giusto, dato che due carte number hanno sempre il type numbbers, allora puoi giocare un 6 rosso su un 7 blu
    * */
    public boolean isPlayableOn(Card topCard){
        //Carte wild
        if (this.color == Color.WILD) {
            return true;
        }

        //Colori
        if (this.color == topCard.getcolor()) {
            return true;
        }
        //numeriche
        if (this.type == CardType.NUMBERS && topCard.getType() == CardType.NUMBERS) {
            return this.value == topCard.getValue();
        }
        //Speciali
        if (this.type != CardType.NUMBERS && this.type == topCard.getType()) {
            return true;
        }

        //ILLEGALI
        return false;


        /*return this.color == topCard.color ||
                this.color == Color.WILD ||
                this.value == topCard.value ||
                this.type == topCard.type; */
    }

}
