package org.openjfx.model;

import java.io.Serializable;
import java.util.*;

public abstract class Player implements Serializable {
/**
 * Defines the player class, complete with name, stats and hand
 * @author leon445
 */
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

    /**
     * Gives the starting hand to the player
     * @param cards cards take from the initialized draw pile
     */
    public void initiateHand(Stack<Card> cards){
        hand = new LinkedList<>();
        hand.addAll(cards);
    }

}


