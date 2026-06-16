package org.openjfx.model;

import java.io.Serializable;

/**
 * Serves to distinguish instances human players from instances of bot players.<br>
 * Extends from Player.
 */
public class HumanPlayer extends Player implements Serializable {

    //You are a flesh automaton animated by neurotransmitters
    public HumanPlayer(String name){
        super.setName(name);
    }
}