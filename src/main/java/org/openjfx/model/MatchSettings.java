package org.openjfx.model;

import java.util.ArrayList;
import java.util.List;

public class MatchSettings {
    public boolean PointsBasedGame = false;
    public int WinningScore = 6700000 ;
    public List<Player> Players = new ArrayList<>();

    public boolean stackingEnabled = false;
    public boolean numberRushEnabled = false;
    public boolean sevenZeroEnabled = false;

    //Altre impostazioni dopo
}