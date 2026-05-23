package org.openjfx.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openjfx.model.Card;
import org.openjfx.model.MatchSettings;
import org.openjfx.model.Player;

import java.util.List;

public class TavoloController implements GameView {

    @FXML private HBox playerHandBox;
    @FXML private StackPane tableArea;
    @FXML private Label lblMessage;
    @FXML private Label lblCurrentPlayer;


    @FXML
    public void initialize() {
        renderDeck();
    }

    public void setInitialData(MatchSettings settings) {
        System.out.println("Match started with " + settings.Players.size() + " players");
    }

    // istanze di gameENGINE da qui ^^^


    @Override
    public void updateTopCard(Card topCard) {
        tableArea.getChildren().removeIf(node -> node.getTranslateX() > 0);

        Rectangle cardNode = createCardNode(topCard);
        cardNode.setTranslateX(50);

        tableArea.getChildren().add(cardNode);
    }

    @Override
    public void updatePlayerHand(List<Card> hand) {
        playerHandBox.getChildren().clear();

        for (Card card : hand) {
            Rectangle cardNode = createCardNode(card);

            cardNode.setOnMouseClicked(e -> {
                System.out.println("Card clicked: " + card.getType());
            });

            playerHandBox.getChildren().add(cardNode);
        }

        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setSpacing(-20);
    }

    @Override
    public void showMessage(String msg) {
        lblMessage.setText(msg);
    }

    @Override
    public void onTurnChanged(Player currentPlayer) {
        lblCurrentPlayer.setText("Current turn: " + currentPlayer.name);
    }

//SEGNALI DELLA UI QUI
    private void onDeckClicked() {
        System.out.println("Deck pile clicked.");
        // engine.drawCardForPlayer();
    }

// GRAFICA
    private Rectangle createCardNode(Card card) {
        Rectangle rect = new Rectangle(70, 105);
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(3);
        rect.setArcWidth(15);
        rect.setArcHeight(15);

        switch (card.getcolor()) {
            case RED -> rect.setFill(Color.web("#d12a2a"));
            case BLUE -> rect.setFill(Color.web("#2a6cd1"));
            case GREEN -> rect.setFill(Color.web("#2ad14b"));
            case YELLOW -> rect.setFill(Color.web("#f0c816"));
            case WILD -> rect.setFill(Color.web("#4b0082"));
        }
        return rect;
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