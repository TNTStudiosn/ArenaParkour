package com.TNTStudios.arenaParkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckpointCommand implements CommandExecutor {

    private final ArenaParkour plugin;
    private final Map<UUID, Location> playerCheckpoints = new HashMap<>();

    public CheckpointCommand(ArenaParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Solo puede usarse si quien lo ejecuta es un jugador
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Solo OPS
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        // Verificar qué comando se ha ejecutado
        String cmdName = command.getName();
        if (cmdName.equalsIgnoreCase("checkpoint")) {
            guardarCheckpoint(player, playerUUID);
        } else if (cmdName.equalsIgnoreCase("regresar")) {
            regresarCheckpoint(player, playerUUID);
        }

        return true;
    }

    // --------------------------------------------------------------------------------
    //                               /CHECKPOINT
    // --------------------------------------------------------------------------------
    private void guardarCheckpoint(Player player, UUID uuid) {
        Location currentLoc = player.getLocation();

        // Si el jugador ya tiene un checkpoint, comprobamos si el nuevo está
        // dentro de un rango de 4 bloques en x, y, z.
        if (playerCheckpoints.containsKey(uuid)) {
            Location oldLoc = playerCheckpoints.get(uuid);

            // Comprobamos diferencias en cada eje
            double diffX = Math.abs(currentLoc.getX() - oldLoc.getX());
            double diffY = Math.abs(currentLoc.getY() - oldLoc.getY());
            double diffZ = Math.abs(currentLoc.getZ() - oldLoc.getZ());

            // Si está dentro del cubo ±4 en cada eje, NO se guarda nada
            if (diffX <= 4 && diffY <= 4 && diffZ <= 4) {
                // No mostrar mensaje ni guardar
                return;
            }
        }

        // Guardamos la ubicación como nuevo checkpoint
        playerCheckpoints.put(uuid, currentLoc);

        // Mostramos un título de checkpoint guardado
        player.sendTitle(
                ChatColor.GREEN + "¡Checkpoint guardado!",
                ChatColor.YELLOW + "Sigue avanzando...",
                10, 20, 10
        );

        // Sonido
        player.playSound(currentLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    // --------------------------------------------------------------------------------
    //                               /REGRESAR
    // --------------------------------------------------------------------------------
    private void regresarCheckpoint(Player player, UUID uuid) {
        // Verificamos si hay un checkpoint guardado
        if (!playerCheckpoints.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "No tienes ningún checkpoint guardado.");
            return;
        }

        // Teletransportamos al jugador a su último checkpoint
        Location checkpointLoc = playerCheckpoints.get(uuid);
        player.teleport(checkpointLoc);

        // Mostramos título de regreso
        player.sendTitle(
                ChatColor.RED + "Casi...",
                ChatColor.YELLOW + "¡Sigue intentándolo!",
                10, 20, 10
        );

        // Sonido de feedback
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }
}
