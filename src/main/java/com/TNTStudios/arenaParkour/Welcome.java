package com.TNTStudios.arenaParkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Welcome implements Listener {

    private final ArenaParkour plugin;

    public Welcome(ArenaParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Teletransportar al jugador al spawn del lobby con rotación -90° (mirando a la izquierda)
        World world = player.getWorld();
        Location spawnLocation = new Location(world, -4.53, -38, 26.53, -90, 0);
        player.teleport(spawnLocation);

        // Aplicar el efecto de visión nocturna sin partículas
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));

        // Enviar título y subtítulo de bienvenida
        player.sendTitle("§6Arena", "§eThe place to play", 20, 90, 20);

        // Reproducir sonido de bienvenida
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        // Mostrar scoreboard con el mejor tiempo
        plugin.getBestTimeManager().showScoreboard(player);
    }
}