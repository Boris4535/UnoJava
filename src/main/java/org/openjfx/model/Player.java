package org.openjfx.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public abstract class Player {
    public String name;
    private List<Card> hand = new LinkedList<>();
    private HashMap<CardType, Integer> points;

    public void receiveCard(Card c){hand.add(c);}
    public void removeCard(Card c){hand.remove(c);}
    public int getHandSize(){ return hand.size();}
    public boolean hasUno(){ return hand.size() == 1;}
    public List<Card> getHand(){ return hand;}


    public void initiateHand(Stack<Card> cards){
        hand = new LinkedList<>();
        hand.addAll(cards);
    }

}


