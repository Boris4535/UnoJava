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
import org.openjfx.model.HumanPlayer;
import org.openjfx.model.MatchSettings;
import org.openjfx.model.Player;

public class SetupController {
    @FXML private ToggleButton togglePunti;
    @FXML private TextField    txtSoglia;
    @FXML private CheckBox     chkStacking;
    @FXML private CheckBox     chkNumberRush;
    @FXML private CheckBox     chkSevenZero;
    @FXML private VBox         playersContainer;

    public static final int DEFAULT_PLAYERS = 2;

    @FXML
    public void initialize() {
        togglePunti.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtSoglia.setDisable(!newVal);
            togglePunti.setText(newVal ? "Partita a Punti" : "Partita Singola");
        });
        txtSoglia.setDisable(true);

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
            //Bot player lo aggiungo dopo che LUCIA aggiunge ir suo
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