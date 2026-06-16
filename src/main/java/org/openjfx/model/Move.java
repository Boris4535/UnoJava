package org.openjfx.model;
import java.io.Serializable;

//Storico mosse

public class Move implements Serializable {
    public String playerName;
    public String actionDescription;

    public Move(String playerName, String actionDescription) {
        this.playerName = playerName;
        this.actionDescription = actionDescription;
    }

    @Override
    public String toString() {
        return playerName + ": " + actionDescription;
    }
}