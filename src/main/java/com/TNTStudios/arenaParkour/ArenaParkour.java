package com.TNTStudios.arenaParkour;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaParkour extends JavaPlugin {

    @Override
    public void onEnable() {
        // Listener de bienvenida
        getServer().getPluginManager().registerEvents(new Welcome(this), this);

        // TimerCommand
        TimerCommand timerCommand = new TimerCommand(this);
        getCommand("empezar").setExecutor(timerCommand);
        getCommand("detener").setExecutor(timerCommand);

        // CheckpointCommand
        CheckpointCommand checkpointCommand = new CheckpointCommand(this);
        getCommand("checkpoint").setExecutor(checkpointCommand);
        getCommand("regresar").setExecutor(checkpointCommand);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
