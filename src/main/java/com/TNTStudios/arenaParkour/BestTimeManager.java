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
    // Almacena el mejor tiempo de cada jugador (en segundos).
    private final Map<UUID, Integer> bestTimes = new HashMap<>();

    public BestTimeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ----------------------------------------------------------------------
    //                          CARGAR / GUARDAR
    // ----------------------------------------------------------------------
    /**
     * Carga los mejores tiempos desde el config.yml al iniciar el plugin.
     */
    public void loadBestTimes() {
        // Asegurarnos de que el config esté cargado
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("bestTimes");
        if (section != null) {
            for (String uuidString : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int time = section.getInt(uuidString);
                    bestTimes.put(uuid, time);
                } catch (IllegalArgumentException e) {
                    // Si el UUID es inválido en el config, lo ignoramos
                    plugin.getLogger().warning("UUID inválido en config: " + uuidString);
                }
            }
        }
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos cargados!");
    }

    /**
     * Guarda los mejores tiempos en el config.yml al deshabilitar el plugin.
     */
    public void saveBestTimes() {
        for (Map.Entry<UUID, Integer> entry : bestTimes.entrySet()) {
            UUID uuid = entry.getKey();
            int time = entry.getValue();
            plugin.getConfig().set("bestTimes." + uuid.toString(), time);
        }
        plugin.saveConfig();
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos guardados!");
    }

    // ----------------------------------------------------------------------
    //                           MÉTODOS PRINCIPALES
    // ----------------------------------------------------------------------
    /**
     * Devuelve el mejor tiempo en segundos de un jugador, o -1 si no tiene.
     */
    public int getBestTime(UUID playerUUID) {
        return bestTimes.getOrDefault(playerUUID, -1);
    }

    /**
     * Actualiza el mejor tiempo de un jugador si el nuevo es menor.
     * @param playerUUID El UUID del jugador.
     * @param newTime El tiempo recién obtenido (en segundos).
     * @return true si se actualizó (nuevo récord), false si no.
     */
    public boolean updateBestTime(UUID playerUUID, int newTime) {
        int currentBest = getBestTime(playerUUID);
        // Si no tenía registro o el nuevo tiempo es menor, actualizamos
        if (currentBest == -1 || newTime < currentBest) {
            bestTimes.put(playerUUID, newTime);
            return true; // Nuevo récord
        }
        return false; // No cambió
    }

    /**
     * Muestra un scoreboard al jugador con su mejor tiempo,
     * o indica que aún no ha completado el parkour.
     */
    public void showScoreboard(Player player) {
        // Creamos un scoreboard nuevo
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // Creamos un objetivo tipo 'dummy' para el sidebar
        Objective objective = board.registerNewObjective("ParkourBest", "dummy",
                ChatColor.DARK_AQUA + "Tu Mejor Tiempo");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Obtenemos el mejor tiempo
        UUID uuid = player.getUniqueId();
        int best = getBestTime(uuid);

        // Creamos la línea que muestra el tiempo
        String line;
        if (best == -1) {
            line = ChatColor.YELLOW + "Aún no has hecho un intento";
        } else {
            line = ChatColor.GREEN + "Mejor: " + formatTime(best);
        }

        // Asignamos esa línea a un Score, para que aparezca en el scoreboard
        Score scoreLine = objective.getScore(line);
        scoreLine.setScore(1); // Valor arbitrario para que se muestre

        // Aplicamos el scoreboard al jugador
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
}
