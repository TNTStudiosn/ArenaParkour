package com.TNTStudios.arenaParkour;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class TimerCommand implements CommandExecutor {

    private final ArenaParkour plugin;
    // Usamos UUID como llave para evitar problemas con referencias a Player.
    private final Map<UUID, Integer> playerTimers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> timerTasks = new HashMap<>();

    public TimerCommand(ArenaParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificamos que el ejecutor sea un jugador
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();

        // Verificamos permisos
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        String cmdName = command.getName(); // Usamos el nombre real del comando (evita conflictos con alias)

        // ---------------------------------------------------------------------
        //                             /EMPEZAR
        // ---------------------------------------------------------------------
        if (cmdName.equalsIgnoreCase("empezar")) {


            // Si ya existe una tarea para este jugador, significa que ya ha iniciado
            if (timerTasks.containsKey(playerUUID)) {
                player.sendMessage(ChatColor.RED + "El contador ya está en marcha.");
                return true;
            }

            // Iniciamos el tiempo en 0
            playerTimers.put(playerUUID, 0);

            // Mensaje y sonido de inicio
            player.sendTitle(ChatColor.GOLD + "Contador iniciado", "", 10, 70, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

            // Creamos la tarea de conteo
            BukkitRunnable timerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Verificamos que el jugador aún tenga un tiempo registrado
                    if (!playerTimers.containsKey(playerUUID)) {
                        this.cancel();
                        timerTasks.remove(playerUUID);
                        return;
                    }

                    // Incrementamos en 1 segundo
                    int timeElapsed = playerTimers.get(playerUUID) + 1;
                    playerTimers.put(playerUUID, timeElapsed);


                    // Mostramos el tiempo transcurrido en el Action Bar
                    String formattedTime = formatTime(timeElapsed);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.GREEN + "Tiempo transcurrido: " + formattedTime));
                }
            };

            // Iniciamos la tarea con 1 seg de delay y se repite cada 1 seg
            timerTask.runTaskTimer(plugin, 20, 20);
            timerTasks.put(playerUUID, timerTask);

            // ---------------------------------------------------------------------
            //                             /DETENER
            // ---------------------------------------------------------------------
        } else if (cmdName.equalsIgnoreCase("detener")) {

            // Verificamos si hay una tarea en ejecución para el jugador
            if (!timerTasks.containsKey(playerUUID)) {
                player.sendMessage(ChatColor.RED + "El contador no está en marcha.");
                return true;
            }

            // Cancelamos la tarea
            timerTasks.get(playerUUID).cancel();
            timerTasks.remove(playerUUID);

            // Verificamos si existe un tiempo almacenado
            if (!playerTimers.containsKey(playerUUID)) {
                player.sendMessage(ChatColor.RED + "No hay tiempo registrado para detener.");
                return true;
            }

            // Obtenemos y removemos el tiempo
            int totalTime = playerTimers.get(playerUUID);
            String finalTime = formatTime(totalTime);
            playerTimers.remove(playerUUID);

            // Mensaje y sonido de fin
            player.sendTitle(ChatColor.RED + "Tiempo detenido",
                    ChatColor.YELLOW + "Tardaste " + finalTime + " en pasar el parkour", 10, 70, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);

            // Actualizamos best time si es menor y mostramos scoreboard
            plugin.getBestTimeManager().updateBestTime(playerUUID, totalTime);  // <-- Cambio
            plugin.getBestTimeManager().showScoreboard(player);
            plugin.getBestTimeManager().saveBestTimes();

        }

        return true;
    }

    // Método para formatear el tiempo en HH:MM:SS
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, secs);
    }
}
