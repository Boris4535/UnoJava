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
    @FXML private Spinner<Integer> spinnerPlayers;

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
    }

    public void buildPlayerRows(int count) {
        playersContainer.getChildren().clear();
        for (int i = 0; i < count; i++) {
            Label label = new Label("Giocatore " + (i + 1));
            label.setPrefWidth(90);

            TextField txtNome = new TextField("Giocatore " + (i + 1));
            txtNome.setPrefWidth(150);

            ComboBox<String> cmbTipo = new ComboBox<>();
            cmbTipo.getItems().addAll("Umano", "Bot");
            cmbTipo.setValue("Umano");

            HBox row = new HBox(10, label, txtNome, cmbTipo);
            playersContainer.getChildren().add(row);
        }
    }

    public List<Player> readPlayers() {
        List<Player> players = new ArrayList<>();
        for (var node : playersContainer.getChildren()) {
            HBox row = (HBox) node;
            String nome = ((TextField) row.getChildren().get(1)).getText().trim();
            String tipo = ((ComboBox<?>) row.getChildren().get(2)).getValue().toString();

            if (nome.isEmpty()) nome = "Giocatore " + (players.size() + 1);

            players.add(new HumanPlayer(nome));
            if (tipo.equals("Bot")) {
                // Per ora mettiamo STUPID di default, poi se vuoi puoi aggiungere una ComboBox per la difficoltà
                BotPlayer bot = new BotPlayer(BotType.STUPID);
                bot.setName(nome + " [BOT]");
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

        FXMLLoader loader = App.getLoader("Tavolo");
        TavoloController tavoloController = loader.getController();
        tavoloController.setInitialData(settings);
    }

    @FXML
    public void goBack() throws IOException {
        App.setRoot("Primary");
    }
}