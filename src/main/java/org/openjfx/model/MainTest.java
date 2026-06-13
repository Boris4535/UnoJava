package org.openjfx.model;

import org.openjfx.controller.GameView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        Card c = new Card(Color.BLUE,CardType.NUMBERS,2);
        System.out.println(c.getValue());

        Player lux = new HumanPlayer("Lux");
        Player leon = new HumanPlayer("Leon");
        Player andriy = new HumanPlayer("Andriy");
        List<Player> players = new ArrayList<>();
        players.add(lux);
        players.add(leon);
        players.add(andriy);

        GameMode mode = new ClassicGame();
        GameView view = new GameView() {
            @Override
            public void updateTopCard(Card topCard) {

            }

            @Override
            public void updatePlayerHand(List<Card> hand) {

            }

            @Override
            public void showMessage(String msg) {

            }

            @Override
            public void onTurnChanged(Player currentPlayer) {

            }
        };
        //GameEngine engine = new GameEngine(new GameState(),mode,view);
        lux.receiveCard(new Card(Color.BLUE,CardType.NUMBERS,5));
        lux.receiveCard(new Card(Color.RED,CardType.DRAW_TWO,1));
        lux.receiveCard(new Card(Color.GREEN,CardType.NUMBERS,2));

        leon.receiveCard(new Card(Color.RED,CardType.WILD_JOLLY,1));
        leon.receiveCard(new Card(Color.GREEN,CardType.NUMBERS,9));

        andriy.receiveCard(new Card(Color.GREEN,CardType.NUMBERS,7));

    }
}
