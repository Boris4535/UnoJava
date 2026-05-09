package org.openjfx;

import java.util.List;

public abstract class Player {
    String name;
    List<Card> hand;
    void receiveCard(Card c){
    hand.add(c);
    }
    void removeCard(Card c){
    hand.remove(c);
    }
    int getHandSize(){
    return hand.size();
    }
    boolean hasUno(){
    return hand.size() == 1;
    }
}
