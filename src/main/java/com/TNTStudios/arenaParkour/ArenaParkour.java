package com.TNTStudios.arenaParkour;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaParkour extends JavaPlugin {

    private BestTimeManager bestTimeManager;

    @Override
    public void onEnable() {
        // Carga o crea config.yml
        saveDefaultConfig();
        reloadConfig();

        // Listener de bienvenida
        getServer().getPluginManager().registerEvents(new Welcome(this), this);

        // Inicializamos BestTimeManager y cargamos los tiempos
        bestTimeManager = new BestTimeManager(this);  // <-- Cambio
        bestTimeManager.loadBestTimes();

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
        // Guardamos los mejores tiempos antes de apagar
        bestTimeManager.saveBestTimes();  // <-- Cambio
    }

    // Getter para usarlo desde otras clases (TimerCommand, Welcome, etc.)
    public BestTimeManager getBestTimeManager() {
        return bestTimeManager;  // <-- Cambio
    }
}
