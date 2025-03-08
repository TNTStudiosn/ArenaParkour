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

    // Almacena el último nombre conocido de cada jugador.
    private final Map<UUID, String> bestNames = new HashMap<>();

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

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("bestTimes");
        if (section != null) {
            for (String uuidString : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    // En config, cada jugador tendrá:
                    // bestTimes:
                    //   <uuid>:
                    //     time: 123
                    //     name: "Ejemplo"

                    ConfigurationSection playerSec = section.getConfigurationSection(uuidString);
                    if (playerSec != null) {
                        int time = playerSec.getInt("time", -1);
                        String name = playerSec.getString("name", "Desconocido");

                        bestTimes.put(uuid, time);
                        bestNames.put(uuid, name);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID inválido en config: " + uuidString);
                }
            }
        }
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos (y nombres) cargados!");
    }

    /**
     * Guarda los mejores tiempos (y nombres) en el config.yml al deshabilitar el plugin.
     */
    public void saveBestTimes() {
        for (UUID uuid : bestTimes.keySet()) {
            int time = bestTimes.get(uuid);
            String name = bestNames.getOrDefault(uuid, "Desconocido");

            String path = "bestTimes." + uuid.toString();
            plugin.getConfig().set(path + ".time", time);
            plugin.getConfig().set(path + ".name", name);
        }
        plugin.saveConfig();
        plugin.getLogger().info("[ArenaParkour] ¡Mejores tiempos guardados!");
    }

    // ----------------------------------------------------------------------
    //                           MÉTODOS PRINCIPALES
    // ----------------------------------------------------------------------
    /**
     * Devuelve el mejor tiempo (en segundos) de un jugador, o -1 si no tiene.
     */
    public int getBestTime(UUID playerUUID) {
        return bestTimes.getOrDefault(playerUUID, -1);
    }

    /**
     * Actualiza el mejor tiempo de un jugador si el nuevo es menor (no cambia si es peor).
     */
    public void updateBestTimeWithName(UUID playerUUID, String playerName, int newTime) {
        int currentBest = getBestTime(playerUUID);

        // Si no tenía registro o el nuevo tiempo es menor, lo reemplazamos.
        if (currentBest == -1 || newTime < currentBest) {
            bestTimes.put(playerUUID, newTime);
        }
        // Guardamos/actualizamos también el nombre (en caso de que haya cambiado).
        bestNames.put(playerUUID, playerName);
    }

    /**
     * Devuelve el último nombre conocido de un jugador.
     * Si no existe, retorna "Desconocido".
     */
    public String getLastKnownName(UUID playerUUID) {
        return bestNames.getOrDefault(playerUUID, "Desconocido");
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

        UUID uuid = player.getUniqueId();
        int best = getBestTime(uuid);

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
    public Map<UUID, Integer> getAllTimes() {
        return new HashMap<>(bestTimes);
    }
}
