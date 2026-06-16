package org.openjfx.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.App;
import org.openjfx.model.*;

public class SetupController {
    @FXML private ToggleButton togglePunti;
    @FXML private TextField    txtSoglia;
    @FXML private CheckBox     chkStacking;
    @FXML private CheckBox     chkNumberRush;
    @FXML private CheckBox     chkSevenZero;
    @FXML private VBox         playersContainer;
    @FXML private CheckBox     chkPrivacy;
    @FXML private Spinner<Integer> spinnerPlayers;

    @FXML private HBox boxCustomScores;
    @FXML private Spinner<Integer> spinNumScore;
    @FXML private Spinner<Integer> spinActScore;
    @FXML private Spinner<Integer> spinWildScore;

    @FXML private CheckBox chkSimulation;
    @FXML private TextField txtSimCount;
    @FXML private CheckBox chkCustomScoring;

    public static final int DEFAULT_PLAYERS = 2;

    @FXML
    public void initialize() {
        togglePunti.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtSoglia.setDisable(!newVal);
            togglePunti.setText(newVal ? "Partita a Punti" : "Partita Singola");
        });
        txtSoglia.setDisable(true);

        // Configura lo spinner
        /*Praticamente, parte che poi scrivo nel javadoc.
         l'utilizzo di uno switch case (seleziona il numero di giocatori e in base a quello generiamo) è brutta e poco elegante
         mentre mettere un campo di testo o cose del genere è ancora più cringe ed error prone. << "dammi -67 giocatori hahahaahh!!!>>
         lo spinner, è, come dire, una rotella.  Noi gli diamo an upper and lower bound, ed essa, in base all'input aggiunge e toglie.

         ASSIEME AL LISTENER, esso controlla cosa è cambiato solo quando qualcosa cambia, non sta lì a mangiare memoria su memoria
         controllando cose
         */

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 6, DEFAULT_PLAYERS);
        spinnerPlayers.setValueFactory(valueFactory);

        // Listener: se il numero cambia, rigenera le righe
        spinnerPlayers.valueProperty().addListener((obs, oldVal, newVal) -> {
            buildPlayerRows(newVal);
        });

        buildPlayerRows(DEFAULT_PLAYERS);

        if (chkSimulation != null && txtSimCount != null) {
            txtSimCount.setDisable(true);
            chkSimulation.selectedProperty().addListener((obs, oldVal, newVal) -> {
                txtSimCount.setDisable(!newVal);
                for (var node : playersContainer.getChildren()) {
                    HBox row = (HBox) node;
                    ComboBox<String> cmbTipo = (ComboBox<String>) row.getChildren().get(2);
                    ComboBox<String> cmbBotProfile = (ComboBox<String>) row.getChildren().get(3);

                    if (newVal) {
                        cmbTipo.setValue("Bot");
                        cmbTipo.setDisable(true); // Forza a Bot e blocca
                        cmbBotProfile.setVisible(true); // Mostra i profili
                    } else {
                        cmbTipo.setDisable(false); // Sblocca
                    }
                }
            });
        }

        spinNumScore.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 5));
        spinActScore.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 10));
        spinWildScore.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 25));

        // Mostra i campi solo se il toggle è attivo
        if (chkCustomScoring != null) {
            chkCustomScoring.selectedProperty().addListener((obs, oldVal, newVal) -> {
                boxCustomScores.setVisible(newVal);
                boxCustomScores.setManaged(newVal); // Fa in modo che occupi spazio solo se visibile
            });
        }
    }


    public void buildPlayerRows(int count) {
        playersContainer.getChildren().clear();
        for (int i = 0; i < count; i++) {
            Label label = new Label("Giocatore " + (i + 1));
            label.setPrefWidth(90);

            TextField txtNome = new TextField("Giocatore " + (i + 1));
            txtNome.setPrefWidth(120);

            ComboBox<String> cmbTipo = new ComboBox<>();
            cmbTipo.getItems().addAll("Umano", "Bot");
            cmbTipo.setValue("Umano");

            // Nuova ComboBox per il profilo Bot
            ComboBox<String> cmbBotProfile = new ComboBox<>();
            cmbBotProfile.getItems().addAll("STUPID", "CLEVER", "CHEEKY");
            cmbBotProfile.setValue("STUPID");
            cmbBotProfile.setVisible(false); // Nascosta di default perché si parte da "Umano"
            cmbBotProfile.setPrefWidth(90);

            // Listener per mostrare/nascondere il profilo in base alla scelta
            cmbTipo.setOnAction(e -> {
                cmbBotProfile.setVisible(cmbTipo.getValue().equals("Bot"));
            });

            HBox row = new HBox(10, label, txtNome, cmbTipo, cmbBotProfile);
            playersContainer.getChildren().add(row);
        }
    }

    @FXML
    public void onLoadGameClicked() throws IOException {
        GameState loadedState = org.openjfx.utils.SaveManager.loadGame("Slot1");

        if (loadedState != null) {
            FXMLLoader loader = App.getLoader("Tavolo");
            TavoloController tavoloController = loader.getController();

            tavoloController.loadExistingGame(loadedState);
        } else {
            System.out.println("Nessun salvataggio trovato.");
        }
    }

    public List<Player> readPlayers() {
        List<Player> players = new ArrayList<>();
        for (var node : playersContainer.getChildren()) {
            HBox row = (HBox) node;
            String nome = ((TextField) row.getChildren().get(1)).getText().trim();
            String tipo = ((ComboBox<?>) row.getChildren().get(2)).getValue().toString();

            if (nome.isEmpty()) nome = "Giocatore " + (players.size() + 1);

            if (tipo.equals("Bot")) {
                // Leggiamo il valore della quarta colonna (indice 3)
                String profileString = ((ComboBox<?>) row.getChildren().get(3)).getValue().toString();
                BotType profile = BotType.valueOf(profileString); // Converte la stringa nell'Enum

                BotPlayer bot = new BotPlayer(profile);
                bot.setName(nome + " [" + profileString + "]");
                players.add(bot);
            } else {
                players.add(new HumanPlayer(nome));
            }
        }
        return players;
    }

    @FXML
    public void startGame() throws IOException {
        MatchSettings settings = new MatchSettings();
        settings.PointsBasedGame = togglePunti.isSelected();
        settings.customScoringEnabled = chkCustomScoring != null && chkCustomScoring.isSelected();

        settings.customScoringEnabled = chkCustomScoring != null && chkCustomScoring.isSelected();
        if (settings.customScoringEnabled) {
            settings.customNumberValue = spinNumScore.getValue();
            settings.customActionValue = spinActScore.getValue();
            settings.customWildValue = spinWildScore.getValue();
        }

        if (settings.PointsBasedGame && !txtSoglia.getText().isEmpty()) {
            try {
                settings.WinningScore = Integer.parseInt(txtSoglia.getText());
            } catch (NumberFormatException e) {
                settings.WinningScore = 500;
            }
        }

        settings.stackingEnabled   = chkStacking.isSelected();
        settings.numberRushEnabled = chkNumberRush.isSelected();
        settings.sevenZeroEnabled  = chkSevenZero.isSelected();
        settings.Players           = readPlayers();

        settings.privacyModeEnabled = chkPrivacy != null && chkPrivacy.isSelected();

        settings.simulationModeEnabled = chkSimulation != null && chkSimulation.isSelected();
        if (settings.simulationModeEnabled && txtSimCount != null && !txtSimCount.getText().isEmpty()) {
            try {
                settings.numSimulations = Integer.parseInt(txtSimCount.getText());
            } catch (NumberFormatException e) {
                settings.numSimulations = 100; // Default sicuro
            }
        }

        FXMLLoader loader = App.getLoader("Tavolo");
        TavoloController tavoloController = loader.getController();
        tavoloController.setInitialData(settings);
    }

    @FXML
    public void goBack() throws IOException {
        App.setRoot("Primary");
    }
}