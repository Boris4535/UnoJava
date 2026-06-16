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

    public boolean customScoringEnabled = false;

    //VARIABILI SIMULAZIONE

    public boolean simulationModeEnabled = false;
    public int numSimulations = 1;
    public int customNumberValue = 5;
    public int customActionValue = 10;
    public int customWildValue = 25;
}
