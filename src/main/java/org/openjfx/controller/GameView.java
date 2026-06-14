package org.openjfx.controller;

import org.openjfx.model.Card;
import org.openjfx.model.Player;
import java.util.List;

public interface GameView {
    void updateTopCard(Card topCard);
    void updatePlayerHand(List<Card> hand);
    void showMessage(String msg);
    void onTurnChanged(Player currentPlayer);

    boolean askForChallenge(String challengerName, String victimName);

    org.openjfx.model.Color chooseWildColor();
}