package com.TNTStudios.arenaParkour;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class TimerCommand implements CommandExecutor {

    private final ArenaParkour plugin;
    // Usamos UUID como llave para evitar problemas con referencias a Player.
    private final Map<String, Integer> playerTimers = new HashMap<>();
    private final Map<String, BukkitRunnable> timerTasks = new HashMap<>();

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
            if (timerTasks.containsKey(playerName)) {
                player.sendMessage(ChatColor.RED + "¡Tu Puedes!");
                return true;
            }

            playerTimers.put(playerName, 0);


            // Mensaje y sonido de inicio
            player.sendTitle(ChatColor.GOLD + "Contador iniciado", "", 10, 70, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

            // Creamos la tarea de conteo
            BukkitRunnable timerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Verificamos que el jugador aún tenga un tiempo registrado
                    if (!playerTimers.containsKey(playerName)) {
                        this.cancel();
                        timerTasks.remove(playerName);
                        return;
                    }

                    // Incrementamos en 1 segundo
                    int timeElapsed = playerTimers.get(playerName) + 1;
                    playerTimers.put(playerName, timeElapsed);


                    // Mostramos el tiempo transcurrido en el Action Bar
                    String formattedTime = formatTime(timeElapsed);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.GREEN + "Tiempo transcurrido: " + formattedTime));
                }
            };

            // Iniciamos la tarea con 1 seg de delay y se repite cada 1 seg
            timerTask.runTaskTimer(plugin, 20, 20);
            timerTasks.put(playerName, timerTask);

            // ---------------------------------------------------------------------
            //                             /DETENER
            // ---------------------------------------------------------------------
        } else if (cmdName.equalsIgnoreCase("detener")) {

            // Verificamos si hay una tarea en ejecución para el jugador
            if (!timerTasks.containsKey(playerName)) {
                player.sendMessage(ChatColor.RED + "¡Tu Puedes!");
                return true;
            }

// Cancelamos la tarea
            timerTasks.get(playerName).cancel();
            timerTasks.remove(playerName);


            // Verificamos si existe un tiempo almacenado
            if (!playerTimers.containsKey(playerName)) {
                player.sendMessage(ChatColor.RED + "No hay tiempo registrado para detener.");
                return true;
            }

            // Obtenemos y removemos el tiempo
            int totalTime = playerTimers.get(playerName);
            plugin.getBestTimeManager().updateBestTimeByName(playerName, totalTime);
            String finalTime = formatTime(totalTime);
            playerTimers.remove(playerName);

            // Mensaje y sonido de fin
            player.sendTitle(ChatColor.RED + "Tiempo detenido",
                    ChatColor.YELLOW + "Tardaste " + finalTime + " en pasar el parkour", 10, 70, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);

            // Actualizamos best time con nombre si es menor, mostramos scoreboard y guardamos
            plugin.getBestTimeManager().showScoreboard(player);
            plugin.getBestTimeManager().saveBestTimes();
            plugin.getTablaCommand().actualizarTodasLasTablas();

            // Teletransportar al jugador al spawn del lobby con rotación -90° (mirando a la izquierda)
            World world = player.getWorld();
            Location spawnLocation = new Location(world, -4.53, -38, 26.53, -90, 0);
            player.teleport(spawnLocation);
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
