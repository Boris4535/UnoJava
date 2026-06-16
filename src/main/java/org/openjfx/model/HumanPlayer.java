package org.openjfx.model;

import java.io.Serializable;

public class HumanPlayer extends Player implements Serializable {

    //You are a flesh automaton animated by neurotransmitters
    public HumanPlayer(String name){
        super.setName(name);
    }
}