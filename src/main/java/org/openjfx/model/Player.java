package org.openjfx.model;

import java.util.*;

public abstract class Player {
    public String name;
    private List<Card> hand = new LinkedList<>();
    private boolean hasCalledUno = false;
    private PlayerStats playerStats = new PlayerStats();

    public void receiveCard(Card c){hand.add(c);}
    public void removeCard(Card c){hand.remove(c);}
    public int getHandSize(){ return hand.size();}
    public boolean hasUno(){ return hand.size() == 1;}
    public List<Card> getHand(){ return hand;}
    public String getName(){    return this.name;   }
    public void setName(String nName){ this.name = nName; }
    public boolean getHasCalledUno() { return this.hasCalledUno; }
    public void setHasCalledUno(boolean bool) { this.hasCalledUno = bool; }

    public PlayerStats getStats(){ return this.playerStats; }
    public void registerMatch(boolean hasWon, int winningScore, int numPenalties, int numChallenges){
        this.getStats().registerMatch(hasWon,winningScore,numPenalties,numPenalties);
    }

    public void initiateHand(Stack<Card> cards){
        hand = new LinkedList<>();
        hand.addAll(cards);
    }

}


