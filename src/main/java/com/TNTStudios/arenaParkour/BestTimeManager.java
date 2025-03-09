package com.TNTStudios.arenaParkour;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maneja los mejores tiempos de cada jugador y muestra un scoreboard.
 */
public class BestTimeManager {
    private final JavaPlugin plugin;

    // Ahora usamos nombres en lugar de UUIDs
    private final Map<String, Integer> bestTimesByName = new HashMap<>();

    public BestTimeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ----------------------------------------------------------------------
    //                          CARGAR / GUARDAR
    // ----------------------------------------------------------------------
    /**
     * Carga los mejores tiempos (y nombres) desde el config.yml al iniciar el plugin.
     */
    public void loadBestTimes() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("bestTimesByName");
        if (section != null) {
            for (String playerName : section.getKeys(false)) {
                int time = section.getInt(playerName, -1);
                bestTimesByName.put(playerName, time);
            }
        }
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos cargados usando nombres!");
    }


    /**
     * Guarda los mejores tiempos (y nombres) en el config.yml al deshabilitar el plugin.
     */
    public void saveBestTimes() {
        for (String playerName : bestTimesByName.keySet()) {
            int time = bestTimesByName.get(playerName);
            plugin.getConfig().set("bestTimesByName." + playerName, time);
        }
        plugin.saveConfig();
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos guardados con nombres!");
    }


    // ----------------------------------------------------------------------
    //                           MÉTODOS PRINCIPALES
    // ----------------------------------------------------------------------
    /**
     * Devuelve el mejor tiempo (en segundos) de un jugador, o -1 si no tiene.
     */
    public int getBestTimeByName(String playerName) {
        return bestTimesByName.getOrDefault(playerName, -1);
    }


    /**
     * Actualiza el mejor tiempo de un jugador si el nuevo es menor (no cambia si es peor).
     */
    public void updateBestTimeByName(String playerName, int newTime) {
        int currentBest = getBestTimeByName(playerName);
        if (currentBest == -1 || newTime < currentBest) {
            bestTimesByName.put(playerName, newTime);
        }
    }

    /**
     * Muestra un scoreboard al jugador con su mejor tiempo o "Aún no has hecho un intento".
     */
    public void showScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective(
                "ParkourBest",
                "dummy",
                ChatColor.DARK_AQUA + "Tu Mejor Tiempo"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String playerName = player.getName();
        int best = getBestTimeByName(playerName);


        String line;
        if (best == -1) {
            line = ChatColor.YELLOW + "Aún no has hecho un intento";
        } else {
            line = ChatColor.GREEN + formatTime(best);
        }

        Score scoreLine = objective.getScore(line);
        scoreLine.setScore(1);

        player.setScoreboard(board);
    }

    // ----------------------------------------------------------------------
    //                        MÉTODO AUXILIAR
    // ----------------------------------------------------------------------
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, secs);
    }

    /**
     * Retorna una copia de todos los tiempos, para usarlos en la tabla.
     */
    public Map<String, Integer> getAllTimesByName() {
        return new HashMap<>(bestTimesByName);
    }
}
