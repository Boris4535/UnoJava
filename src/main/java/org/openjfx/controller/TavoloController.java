package org.openjfx.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openjfx.model.*;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;  //Rimuovo i non usati post testing
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;

import java.util.List;

public class TavoloController implements GameView {

    @FXML private HBox playerHandBox;
    @FXML private StackPane tableArea;
    @FXML private Label lblMessage;
    @FXML private Label lblCurrentPlayer;
    @FXML private Button btnCallUno;

    //Stavo facendo dei test, maybe will use later
    @FXML private Label faceTop;
    @FXML private Label faceLeft;
    @FXML private Label faceRight;



    private GameEngine engine;

    @FXML
    public void initialize() {
        renderDeck();


    }

    @FXML
    public void onUnoButtonClicked() {
        if (engine != null) {
            engine.humanCallUno();

            btnCallUno.setVisible(false);
        }
    }


    public void setInitialData(MatchSettings settings) {
        System.out.println("Match started with " + settings.Players.size() + " players");

        // Inizializziamo lo stato e gli passiamo i giocatori letti dal setup
        GameState gameState = new GameState(settings.Players);
        //gameState.players = settings.Players;

        // Scegliamo la modalità in base alle impostazioni
        GameMode mode = settings.PointsBasedGame ? new ScoreBasedGame() : new ClassicGame();

        // Creiamo l'engine passando lo stato, la modalità e questa view
        this.engine = new GameEngine(gameState, mode, this, settings);
        engine.startGame();
    }

    @Override
    public void updateTopCard(Card topCard) {
        tableArea.getChildren().removeIf(node -> node.getTranslateX() > 0);
        StackPane cardNode = createCardNode(topCard);
        cardNode.setTranslateX(50);
        tableArea.getChildren().add(cardNode);
    }

    @Override
    public void updatePlayerHand(List<Card> hand) {
        playerHandBox.getChildren().clear();
        for (Card card : hand) {
            StackPane cardNode = createCardNode(card);
          //UI to game engine
            cardNode.setOnMouseClicked(e -> {
                if (engine != null) {
                    engine.humanPlayCard(card);
                }
            });

            playerHandBox.getChildren().add(cardNode);
        }
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setSpacing(-20);
    }

//Ho fatto un casino lol, riordinato nu poc
    @Override
    public void showMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    @Override
    public void onTurnChanged(Player currentPlayer) {
        if (lblCurrentPlayer != null) {
            lblCurrentPlayer.setText("Current turn: " + currentPlayer.name);
        }
    }

    private void onDeckClicked() {
        if (engine != null) {
            engine.humanDrawCard();
        }
    }

    // ----------------------------------L'ARTE------------------------//

    private StackPane createCardNode(Card card) {
        StackPane pane = new StackPane();
        pane.setMaxSize(70, 105);

        Rectangle rect = new Rectangle(70, 105);
        rect.setStroke(Color.web("#00ff00"));
        rect.setStrokeWidth(4);
        rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);

        switch (card.getColor()) {
            case RED -> rect.setFill(Color.web("#d12a2a"));
            case BLUE -> rect.setFill(Color.web("#2a6cd1"));
            case GREEN -> rect.setFill(Color.web("#2ad14b"));
            case YELLOW -> rect.setFill(Color.web("#f0c816"));
            case WILD -> rect.setFill(Color.web("#8b008b"));


        }
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00ff00"));
        glow.setRadius(20);
        glow.setSpread(0.8);
        rect.setEffect(glow);

        javafx.scene.text.Text text = new javafx.scene.text.Text();
        text.setFont(javafx.scene.text.Font.font("Impact", javafx.scene.text.FontWeight.BOLD, 32));
        text.setFill(Color.web("#ffff00"));
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1.5);

        // Texto
        switch(card.getType()) {
            case NUMBERS -> text.setText(String.valueOf(card.getValue()));
            case SKIP -> text.setText("Ø");
            case REVERSE -> text.setText("R");
            case DRAW_TWO -> text.setText("+2");
            case WILD_JOLLY -> text.setText("W");
            case WILD_DRAW -> text.setText("+4");
        }

        // CAOS QUI, anche se forse tolgo. It was for funsies in un test
        pane.setOnMouseEntered(e -> {
            pane.setRotate(Math.random() * 10 - 5);
            rect.setStroke(Color.web("#ff00ff"));
        });
        pane.setOnMouseExited(e -> {
            pane.setRotate(0);
            rect.setStroke(Color.web("#00ff00"));
        });

        pane.getChildren().addAll(rect, text);
        return pane;
    }

    private void renderDeck() {
        Rectangle deck = new Rectangle(80, 120);
        deck.setFill(Color.BLACK);
        deck.setStroke(Color.WHITE);
        deck.setStrokeWidth(4);
        deck.setArcWidth(15);
        deck.setArcHeight(15);
        deck.setTranslateX(-50);
        deck.setOnMouseClicked(e -> onDeckClicked());
        tableArea.getChildren().add(deck);
    }
}

