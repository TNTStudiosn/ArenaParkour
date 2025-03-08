package com.TNTStudios.arenaParkour;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaParkour extends JavaPlugin {

    private BestTimeManager bestTimeManager;
    private TablaCommand tablaCommand;

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

        // 1) Creamos la instancia tablaCommand
        tablaCommand = new TablaCommand(this);
        getCommand("creartabla").setExecutor(tablaCommand);
        getCommand("eliminartabla").setExecutor(tablaCommand);

        // Cargamos tablas
        tablaCommand.cargarTablasDesdeConfig();
    }


    @Override
    public void onDisable() {
        bestTimeManager.saveBestTimes();
    }

    public BestTimeManager getBestTimeManager() {
        return bestTimeManager;
    }

    public TablaCommand getTablaCommand() {
        return tablaCommand;
    }
}
