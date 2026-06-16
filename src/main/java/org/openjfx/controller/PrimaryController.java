package org.openjfx.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.openjfx.App;
import org.openjfx.model.GameState;

/**
 * Main menu controller
 */
public class PrimaryController {

    @FXML
    private void beginGame() throws IOException {
        App.setRoot("GameSettings");
    }

    @FXML
    public void onLoadGameClicked() {
        showLoadMenu();
    }

    private void showLoadMenu() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Carica Partita");
        dialog.setHeaderText("Seleziona il salvataggio da ripristinare.");

        // Lista dei salvataggi
        ListView<String> saveList = new ListView<>();
        saveList.getItems().addAll(org.openjfx.utils.SaveManager.getAvailableSaves());
        saveList.setPrefHeight(150);

        Label infoLabel = new Label("Seleziona un salvataggio per i dettagli.");
        infoLabel.setStyle("-fx-text-fill: #ffff00; -fx-font-family: monospace;");

        // Aggiorna i dettagli quando si clicca su un file
        saveList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                infoLabel.setText(org.openjfx.utils.SaveManager.getSaveInfo(newV));
            }
        });

        // Bottone Carica
        Button btnLoad = new Button("Carica Selezionato");
        btnLoad.setOnAction(e -> {
            String selected = saveList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                GameState loadedState = org.openjfx.utils.SaveManager.loadGame(selected);

                if (loadedState != null) {
                    try {
                        dialog.close(); // Chiude il pop-up prima di cambiare scena
                        FXMLLoader loader = App.getLoader("Tavolo");
                        TavoloController tavoloController = loader.getController();
                        tavoloController.loadExistingGame(loadedState);
                    } catch (IOException ex) {
                        System.err.println("Errore nel caricamento dell'interfaccia Tavolo.");
                        ex.printStackTrace();
                    }
                } else {
                    infoLabel.setText(" Impossibile caricare il file (corrotto o obsoleto).");
                }
            }
        });

        VBox layout = new VBox(15, saveList, infoLabel, btnLoad);
        layout.setStyle("-fx-background-color: #2b003b; -fx-padding: 20;");

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL); // Tasto per chiudere senza fare nulla

        // Applica i CSS
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/openjfx/Styles.css").toExternalForm());

        dialog.showAndWait();
    }
}