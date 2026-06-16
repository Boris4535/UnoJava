package org.openjfx.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MatchSettings implements Serializable {
    public boolean PointsBasedGame = false;
    public int WinningScore = 6700000 ;
    public List<Player> Players = new ArrayList<>();

    public boolean stackingEnabled = false;
    public boolean numberRushEnabled = false;
    public boolean sevenZeroEnabled = false;
    public boolean privacyModeEnabled = false;

    //VARIABILI SIMULAZIONE

    public boolean simulationModeEnabled = false;
    public int numSimulations = 1;
}
