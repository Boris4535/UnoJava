package org.openjfx.controller;
import java.io.IOException;
import javafx.fxml.FXML;
import org.openjfx.App;

public class PrimaryController {
    @FXML
    private void beginGame() throws IOException {
        App.setRoot("GameSettings");
    }

    @FXML
    private void goToSettings() throws IOException {
        App.setRoot("Secondary");
    }
}