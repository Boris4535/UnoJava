package org.openjfx.utils;

import org.openjfx.model.GameState;

import java.io.*;

/**
 * Save manager, allows for saving and loading game data.
 */
public class SaveManager {
    private static final String SAVE_FOLDER = "saves/";

    static {
        File dir = new File(SAVE_FOLDER);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     *Saves the current game state.
     * @param state the current game state
     * @param filename the file in which the game is saved
     * @return true if saved properly
     */
    public static boolean saveGame(GameState state, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FOLDER + filename + ".sav"))) {
            out.writeObject(state);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads up a save.
     * @param filename the file in which the game is saved
     * @return
     */
    public static GameState loadGame(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FOLDER + filename + ".sav"))) {
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Ottiene la lista di tutti i file di salvataggio
    public static java.util.List<String> getAvailableSaves() {
        File dir = new File(SAVE_FOLDER);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sav"));
        java.util.List<String> saves = new java.util.ArrayList<>();
        if (files != null) {
            for (File f : files) {
                saves.add(f.getName().replace(".sav", ""));
            }
        }
        return saves;
    }

    // Rinomina un file esistente
    public static boolean renameSave(String oldName, String newName) {
        File oldFile = new File(SAVE_FOLDER + oldName + ".sav");
        File newFile = new File(SAVE_FOLDER + newName + ".sav");
        return oldFile.renameTo(newFile);
    }

    // Estrae i metadati per l'anteprima del salvataggio
    public static String getSaveInfo(String filename) {
        File file = new File(SAVE_FOLDER + filename + ".sav");
        if (!file.exists()) return "File non trovato.";

        GameState state = loadGame(filename);
        if (state == null) return " Salvataggio Corrotto o Incompatibile";

        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(file.lastModified());
        int playersCount = state.players.size();
        String currentTurn = state.getCurrentPlayer().getName();
        String mode = (state.settings != null && state.settings.PointsBasedGame) ? "A Punti" : "Singola";

        return String.format("[%s]\nModalità: %s | Giocatori: %d\nTurno di: %s", date, mode, playersCount, currentTurn);
    }
}