package org.openjfx.utils;

import org.openjfx.model.GameState;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FOLDER = "saves/";

    static {
        File dir = new File(SAVE_FOLDER);
        if (!dir.exists()) dir.mkdirs();
    }

    public static boolean saveGame(GameState state, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FOLDER + filename + ".sav"))) {
            out.writeObject(state);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static GameState loadGame(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FOLDER + filename + ".sav"))) {
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}