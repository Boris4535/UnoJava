package org.openjfx;

import java.util.List;

public abstract class Player {
    public String name;
    public List<Card> hand;

    public void receiveCard(Card c){hand.add(c);}
    public void removeCard(Card c){hand.remove(c);}
    public int getHandSize(){ return hand.size();}
    public boolean hasUno(){ return hand.size() == 1;}

}

class HumanPlayer extends Player{
    public HumanPlayer(String name){
    this.name = name;
    }
}
