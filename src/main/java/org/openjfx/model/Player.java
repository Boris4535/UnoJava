package org.openjfx.model;

import java.util.*;

public abstract class Player {
    private String name;
    private List<Card> hand = new LinkedList<>();
    private int matchScore;
    private boolean hasCalledUno = false;

    public void receiveCard(Card c){hand.add(c);}
    public void removeCard(Card c){hand.remove(c);}
    public int getHandSize(){ return hand.size();}
    public boolean hasUno(){ return hand.size() == 1;}
    public List<Card> getHand(){ return hand;}
    public String getName(){    return this.name;   }
    public int getMatchScore() { return this.matchScore;  }
    public void setMatchScore(int nMatchScore){ this.matchScore = nMatchScore; }
    public void setName(String nName){ this.name = nName; }
    public boolean getHasCalledUno() { return this.hasCalledUno; }
    public void setHasCalledUno(boolean bool) { this.hasCalledUno = bool; }

    public void initiateHand(Stack<Card> cards){
        hand = new LinkedList<>();
        hand.addAll(cards);
    }

    public void updateScore(int nScore){
        this.matchScore += nScore;
    }

}


