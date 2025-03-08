package com.TNTStudios.arenaParkour;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaParkour extends JavaPlugin {

    @Override
    public void onEnable() {
        // Registras tu listener
        getServer().getPluginManager().registerEvents(new Welcome(this), this);

        // Creas UNA SOLA instancia del TimerCommand
        TimerCommand timerCommand = new TimerCommand(this);

        // Asignas la misma instancia a ambos comandos
        getCommand("empezar").setExecutor(timerCommand);
        getCommand("detener").setExecutor(timerCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
