package com.TNTStudios.arenaParkour;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.api.holograms.HologramLine;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class TablaCommand implements CommandExecutor {

    private final ArenaParkour plugin;
    private final Map<Integer, Hologram> hologramasActivos = new HashMap<>();

    public TablaCommand(ArenaParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "¡Solo jugadores pueden usar este comando!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        String cmdName = command.getName();
        if (cmdName.equalsIgnoreCase("creartabla")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Uso: /creartabla <numero>");
                return true;
            }
            try {
                int id = Integer.parseInt(args[0]);
                crearTabla(player, id);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Debes poner un número válido.");
            }
            return true;
        }

        if (cmdName.equalsIgnoreCase("eliminartabla")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Uso: /eliminartabla <numero>");
                return true;
            }
            try {
                int id = Integer.parseInt(args[0]);
                eliminarTabla(player, id);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Debes poner un número válido.");
            }
            return true;
        }
        return false;
    }

    private void crearTabla(Player player, int id) {
        if (hologramasActivos.containsKey(id)) {
            player.sendMessage(ChatColor.RED + "Ya existe una tabla con el número " + id);
            return;
        }

        Location loc = player.getLocation();
        String holoName = "tabla_" + id;

        // Crea el holograma
        Hologram holograma = DHAPI.createHologram(holoName, loc, false);
        hologramasActivos.put(id, holograma);

        // Guardar en config
        guardarTablaEnConfig(id, loc);

        // Header animado (opcional)
        HologramPage page = holograma.getPage(0);
        String headerAnimated = ChatColor.GOLD + "<#ANIM:wave:&6,&e&l>TOP 10 PARKOUR</#ANIM>";
        HologramLine headerLine = new HologramLine(page, page.getNextLineLocation(), headerAnimated);
        page.addLine(headerLine);

        // Cargar top
        cargarTop10EnHolograma(holograma);

        // Feedback para el jugador
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendMessage(ChatColor.GREEN + "¡Tabla " + id + " creada exitosamente!");
    }

    private void eliminarTabla(Player player, int id) {
        if (!hologramasActivos.containsKey(id)) {
            player.sendMessage(ChatColor.RED + "No existe ninguna tabla con el número " + id);
            return;
        }

        Hologram holo = hologramasActivos.get(id);
        holo.delete();
        hologramasActivos.remove(id);

        plugin.getConfig().set("tablas." + id, null);
        plugin.saveConfig();

        player.sendMessage(ChatColor.YELLOW + "Se eliminó la tabla " + id + " correctamente.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
    }

    public void actualizarTodasLasTablas() {
        for (int id : hologramasActivos.keySet()) {
            actualizarTabla(id);
        }
    }

    public void actualizarTabla(int id) {
        Hologram holo = hologramasActivos.get(id);
        if (holo == null) return;

        HologramPage page = holo.getPage(0);
        // Borramos todas las líneas, dejando solo la primera (header)
        while (page.getLines().size() > 1) {
            page.removeLine(page.getLines().size() - 1);
        }
        cargarTop10EnHolograma(holo);
    }

    private void cargarTop10EnHolograma(Hologram holo) {
        HologramPage page = holo.getPage(0);
        if (page == null) return;

        // Obtenemos todos los tiempos (y nombres) del BestTimeManager
        Map<UUID, Integer> mapa = plugin.getBestTimeManager().getAllTimes();
        List<Map.Entry<UUID, Integer>> lista = new ArrayList<>(mapa.entrySet());
        // Orden ascendente por tiempo
        lista.sort(Comparator.comparingInt(Map.Entry::getValue));

        // Llenamos las 10 líneas
        for (int i = 0; i < 10; i++) {
            String linea;
            if (i < lista.size()) {
                Map.Entry<UUID, Integer> e = lista.get(i);
                // Usamos el método getLastKnownName(...) en lugar de getPlayer
                String nombreJugador = plugin.getBestTimeManager().getLastKnownName(e.getKey());
                String tiempoFormateado = formatearTiempo(e.getValue());
                linea = ChatColor.WHITE + "" + (i + 1) + ".- "
                        + ChatColor.AQUA + nombreJugador + ChatColor.GRAY + ": "
                        + ChatColor.GREEN + tiempoFormateado;
            } else {
                // Puesto sin ocupar
                linea = ChatColor.DARK_GRAY + "" + (i + 1) + ".- Puesto aún no ocupado";
            }
            HologramLine newLine = new HologramLine(page, page.getNextLineLocation(), linea);
            page.addLine(newLine);
        }
    }

    private void guardarTablaEnConfig(int id, Location loc) {
        String path = "tablas." + id;
        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", loc.getYaw());
        plugin.getConfig().set(path + ".pitch", loc.getPitch());
        plugin.saveConfig();
    }

    public void cargarTablasDesdeConfig() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("tablas");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                String worldName = plugin.getConfig().getString("tablas." + id + ".world");
                double x = plugin.getConfig().getDouble("tablas." + id + ".x");
                double y = plugin.getConfig().getDouble("tablas." + id + ".y");
                double z = plugin.getConfig().getDouble("tablas." + id + ".z");
                float yaw = (float) plugin.getConfig().getDouble("tablas." + id + ".yaw");
                float pitch = (float) plugin.getConfig().getDouble("tablas." + id + ".pitch");

                if (worldName == null) continue;
                Location loc = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);

                String holoName = "tabla_" + id;
                Hologram holo = DHAPI.createHologram(holoName, loc, false);
                hologramasActivos.put(id, holo);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Clave inválida en 'tablas': " + key);
            }
        }
        // Tras crearlas, actualizamos su contenido
        actualizarTodasLasTablas();
    }

    // Eliminamos la vieja lógica de getPlayer(...) y devolvemos "Desconocido"
    // para en su lugar usar getLastKnownName(...) de BestTimeManager en cargarTop10EnHolograma
    // Queda solo este método para formatear tiempo:
    private String formatearTiempo(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02dh%02dm%02ds", hours, minutes, secs);
    }

    public Map<Integer, Hologram> getHologramasActivos() {
        return hologramasActivos;
    }
}
