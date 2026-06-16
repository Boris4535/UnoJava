package org.openjfx.utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openjfx.model.GameStats;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportManager {
    public static boolean exportStatsToJson(GameStats stats) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "sim_results_" + timestamp + ".json";

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(stats, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}