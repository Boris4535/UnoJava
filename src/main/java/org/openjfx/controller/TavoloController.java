package org.openjfx.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openjfx.engine.GameEngine;
import org.openjfx.model.*;
import javafx.scene.effect.DropShadow;

import java.util.Arrays;
import java.util.Optional;

import javafx.scene.input.KeyCode;
import javafx.scene.control.ButtonBar.ButtonData;

import java.util.List;


public class TavoloController implements GameView {

    @FXML private HBox playerHandBox;
    @FXML private StackPane tableArea;
    @FXML private Label lblMessage;
    @FXML private Label lblCurrentPlayer;
    @FXML private Button btnCallUno;
    @FXML private CheckBox chkPrivacy;

    //Stavo facendo dei test, maybe will use later
    @FXML private Label faceTop;
    @FXML private Label faceLeft;
    @FXML private Label faceRight;

    private StackPane privacyOverlay;
    private Label lblPrivacy;



    private GameEngine engine;

    @FXML
    public void initialize() {
        renderDeck();

        // Inizializzazione dinamica Overlay Privacy
        privacyOverlay = new StackPane();
        privacyOverlay.setStyle("-fx-background-color: #000000E6;"); // Sfondo nero semi-trasparente
        privacyOverlay.setVisible(false);

        VBox privacyBox = new VBox(20);
        privacyBox.setAlignment(Pos.CENTER);
        lblPrivacy = new Label("TURNO DI: ");
        lblPrivacy.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 30px; -fx-font-weight: bold;");

        Button btnShow = new Button("MOSTRA CARTE");
        btnShow.setOnAction(e -> hidePrivacyScreen());
        privacyBox.getChildren().addAll(lblPrivacy, btnShow);
        privacyOverlay.getChildren().add(privacyBox);

        tableArea.getChildren().add(privacyOverlay);

        tableArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) showPauseMenu();
                });
            }
        });

        // Rendi visibile sempre il tasto UNO per evitare la race condition
        btnCallUno.setVisible(true);
    }



    @Override
    public void showPrivacyScreen(Player p) {
        privacyOverlay.setVisible(true);
        privacyOverlay.toFront();
        lblPrivacy.setText("TURNO DI: " + p.getName() + " (Privacy Attiva)");
        playerHandBox.getChildren().clear();
    }

    public void hidePrivacyScreen() {
        privacyOverlay.setVisible(false);
        if (engine != null) {
            updatePlayerHand(engine.getState().getCurrentPlayer().getHand());
            showMessage("È il tuo turno, " + engine.getState().getCurrentPlayer().getName());
        }
    }

    @Override
    public Player choosePlayerToSwapHands(List<Player> players, Player currentPlayer) {
        List<Player> targets = new java.util.ArrayList<>(players);
        targets.remove(currentPlayer);
        ChoiceDialog<Player> dialog = new ChoiceDialog<>(targets.get(0), targets);
        dialog.setTitle("Regola del 7");
        dialog.setHeaderText("Hai giocato un 7!");
        dialog.setContentText("Scegli con chi scambiare la mano:");
        Optional<Player> result = dialog.showAndWait();
        return result.orElse(targets.get(0));
    }

    @Override
    public void showPostMatchScreen(GameStats globalStats, org.openjfx.model.MatchStats matchStats) {
        // La vista intercetta la chiamata dal background thread e la sposta sul thread di JavaFX
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Risultati Finali");
            StringBuilder sb = new StringBuilder();

            if (engine.getState().settings.simulationModeEnabled) {
                alert.setHeaderText("Simulazione Batch Conclusa!");
                sb.append("STATISTICHE AGGREGATE:\n");
                sb.append("Durata Simulazione: ").append(engine.simulationDurationMs).append(" ms\n");
                sb.append("Partite simulate: ").append(globalStats.getNumOfPlayedMatches()).append("\n");
                sb.append("Media Round per partita: ").append(String.format("%.2f", globalStats.getAvgNumOfRounds())).append("\n");
                sb.append("Media Turni per round: ").append(String.format("%.2f", globalStats.getAvgNumOfTurns())).append("\n");
                sb.append("Totale Penalità applicate: ").append(globalStats.getNumOfPenalties()).append("\n\n");
                sb.append("VITTORIE PER GIOCATORE/BOT:\n");
                for (Player p : engine.getState().players) {
                    sb.append("- ").append(p.getName()).append(": ")
                            .append(p.getStats().getTotalWonMatches()).append(" vittorie (")
                            .append(p.getStats().getOverallScore()).append(" pt totali)\n");
                }
            } else {
                alert.setHeaderText("La partita è conclusa!");
                sb.append("Vincitore: ").append(engine.getState().getCurrentPlayer().getName()).append("\n\n");
                sb.append("STORICO MOSSE (Ultimo Round):\n");
                for (org.openjfx.model.Move m : matchStats.getMoveHistory()) {
                    sb.append("- ").append(m.toString()).append("\n");
                }
            }

            javafx.scene.control.TextArea area = new javafx.scene.control.TextArea(sb.toString());
            area.setEditable(false);
            area.setWrapText(true);
            alert.getDialogPane().setContent(area);

            ButtonType btnExport = new ButtonType("Esporta in JSON");
            ButtonType btnMenu = new ButtonType("Torna al Menu");
            alert.getButtonTypes().setAll(btnExport, btnMenu);

            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get() == btnExport) {
                if (org.openjfx.utils.ExportManager.exportStatsToJson(globalStats)) {
                    showMessage("Esportato JSON con successo nella cartella del progetto!");
                } else {
                    showMessage("Errore durante l'esportazione JSON.");
                }
            }

            try {
                org.openjfx.App.setRoot("Primary");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onUnoButtonClicked() {
        if (engine != null) {
            engine.handleUnoButtonClick();

            btnCallUno.setVisible(false);
        }
    }


    public void setInitialData(MatchSettings settings) {
        System.out.println("Match started with " + settings.Players.size() + " players");
        GameState gameState = new GameState(settings.Players, settings);
        GameMode mode = settings.PointsBasedGame ? new ScoreBasedGame() : new ClassicGame();

        this.engine = new GameEngine(gameState, mode, this, settings);

        if (settings.simulationModeEnabled) {
            lblMessage.setText("SIMULAZIONE BATCH IN CORSO (" + settings.numSimulations + " partite)...");
            btnCallUno.setVisible(false);
            tableArea.getChildren().clear();

            new Thread(() -> {
                engine.runSimulation(settings.numSimulations);
            }).start();
        } else {
            engine.startGame();
        }
    }

    @Override
    public void updateTopCard(Card topCard) {
        if (engine != null && engine.getState().settings.simulationModeEnabled) return;
        tableArea.getChildren().removeIf(node -> node.getTranslateX() > 0);
        StackPane cardNode = createCardNode(topCard);
        cardNode.setTranslateX(50);
        tableArea.getChildren().add(cardNode);
    }

    @Override
    public void updatePlayerHand(List<Card> hand) {
        if (engine != null && engine.getState().settings.simulationModeEnabled) return;
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
        if (engine != null && engine.getState().settings.simulationModeEnabled) return;
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    @Override
    public void onTurnChanged(Player currentPlayer) {
        if (engine != null && engine.getState().settings.simulationModeEnabled) return;
        if (lblCurrentPlayer != null) {
            lblCurrentPlayer.setText("Current turn: " + currentPlayer.getName());
        }
    }

    @Override
    public org.openjfx.model.Color chooseWildColor() {
        // Opzioni da mostrare
        List<String> choices = Arrays.asList("ROSSO", "BLU", "VERDE", "GIALLO");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("ROSSO", choices);
        dialog.setTitle("Cambio Colore");
        dialog.setHeaderText("Hai giocato una carta Wild!");
        dialog.setContentText("Scegli il nuovo colore:");

        // showAndWait() blocca il gioco finché l'utente non sceglie!
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()){
            switch(result.get()){
                case "ROSSO": return org.openjfx.model.Color.RED;
                case "BLU": return org.openjfx.model.Color.BLUE;
                case "VERDE": return org.openjfx.model.Color.GREEN;
                case "GIALLO": return org.openjfx.model.Color.YELLOW;
            }
        }
        return org.openjfx.model.Color.RED; // Fallback di sicurezza
    }
    private void onDeckClicked() {
        if (engine != null) {
            engine.humanDrawCard();
        }
    }

    @Override
    public boolean askForChallenge(String challengerName, String victimName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Challenge +4!");
        alert.setHeaderText(challengerName + " ti ha appena tirato un +4!");
        alert.setContentText("Vuoi contestare la giocata? Se " + challengerName + " ha una carta dello stesso colore di prima in mano, pesca lui 4 carte. Se ti sbagli, ne peschi 6 tu!");

        ButtonType btnYes = new ButtonType("Contesta!");
        ButtonType btnNo = new ButtonType("Accetta Penalità");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }

    public void loadExistingGame(GameState loadedState) {
        System.out.println("Ripristino della partita in corso...");

        MatchSettings savedSettings = loadedState.settings;
        if (savedSettings == null) {
            // Fallback estremo se il salvataggio è corrotto o vecchio
            savedSettings = new MatchSettings();
        }

        GameMode mode = savedSettings.PointsBasedGame ? new ScoreBasedGame() : new ClassicGame();

        this.engine = new GameEngine(loadedState, mode, this, savedSettings);

        this.engine.resumeGame();
    }

    private void showPauseMenu() {
        Alert pause = new Alert(Alert.AlertType.NONE);
        pause.setTitle("Pausa");
        pause.setHeaderText("GIOCO IN PAUSA");
        pause.setContentText("Cosa vuoi fare?");

        ButtonType btnSave = new ButtonType("Salva Partita");
        ButtonType btnExit = new ButtonType("Esci al Menu");
        ButtonType btnResume = new ButtonType("Riprendi", ButtonData.CANCEL_CLOSE);

        pause.getButtonTypes().setAll(btnSave, btnExit, btnResume);

        Optional<ButtonType> result = pause.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnSave) {
                showAdvancedSaveMenu();
            } else if (result.get() == btnExit) {
                try {
                    org.openjfx.App.setRoot("Primary");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showAdvancedSaveMenu() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gestione Salvataggi");
        dialog.setHeaderText("Seleziona uno slot, rinominalo o creane uno nuovo.");

        // Componenti UI
        ListView<String> saveList = new ListView<>();
        saveList.getItems().addAll(org.openjfx.utils.SaveManager.getAvailableSaves());
        saveList.setPrefHeight(150);

        Label infoLabel = new Label("Seleziona un salvataggio per i dettagli.");
        infoLabel.setStyle("-fx-text-fill: #ffff00; -fx-font-family: monospace;");

        TextField txtNewName = new TextField();
        txtNewName.setPromptText("Nome nuovo salvataggio...");

        // Listener: Quando si clicca su un salvataggio nella lista, mostra le info e prepara il testo
        saveList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtNewName.setText(newV);
                infoLabel.setText(org.openjfx.utils.SaveManager.getSaveInfo(newV));
            }
        });

        // Bottone Salva / Sovrascrivi
        Button btnSaveNew = new Button("Salva / Sovrascrivi");
        btnSaveNew.setOnAction(e -> {
            String name = txtNewName.getText().trim();
            if (!name.isEmpty()) {
                org.openjfx.utils.SaveManager.saveGame(engine.getState(), name);
                saveList.getItems().setAll(org.openjfx.utils.SaveManager.getAvailableSaves());
                infoLabel.setText("Partita salvata con successo in: " + name);
            }
        });

        // Bottone Rinomina
        Button btnRename = new Button("Rinomina Selezionato");
        btnRename.setOnAction(e -> {
            String selected = saveList.getSelectionModel().getSelectedItem();
            String newName = txtNewName.getText().trim();
            if (selected != null && !newName.isEmpty() && !selected.equals(newName)) {
                if (org.openjfx.utils.SaveManager.renameSave(selected, newName)) {
                    saveList.getItems().setAll(org.openjfx.utils.SaveManager.getAvailableSaves());
                    saveList.getSelectionModel().select(newName);
                    infoLabel.setText("Rinominato con successo.");
                }
            }
        });

        // Layout
        HBox buttons = new HBox(10, btnSaveNew, btnRename);
        VBox layout = new VBox(15, saveList, infoLabel, txtNewName, buttons);
        layout.setStyle("-fx-background-color: #2b003b; -fx-padding: 20;");

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/openjfx/Styles.css").toExternalForm());

        dialog.showAndWait();
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

