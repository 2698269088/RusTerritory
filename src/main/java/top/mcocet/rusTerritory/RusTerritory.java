package top.mcocet.rusTerritory;

import org.bukkit.plugin.java.JavaPlugin;
import top.mcocet.rusTerritory.commands.RusCommand;
import top.mcocet.rusTerritory.commands.RutCommand;
import top.mcocet.rusTerritory.config.ConfigManager;
import top.mcocet.rusTerritory.listeners.SelectionListener;
import top.mcocet.rusTerritory.listeners.TerritoryListener;
import top.mcocet.rusTerritory.storage.TerritoryStorage;

public final class RusTerritory extends JavaPlugin {
    private ConfigManager configManager;
    private TerritoryStorage storage;
    private SelectionListener selectionListener;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.storage = new TerritoryStorage(this);
        this.selectionListener = new SelectionListener(this);

        RutCommand rutCommand = new RutCommand(this);
        getCommand("rut").setExecutor(rutCommand);
        getCommand("rut").setTabCompleter(rutCommand);

        RusCommand rusCommand = new RusCommand(this);
        getCommand("rus").setExecutor(rusCommand);
        getCommand("rus").setTabCompleter(rusCommand);

        getServer().getPluginManager().registerEvents(new TerritoryListener(this), this);
        getServer().getPluginManager().registerEvents(selectionListener, this);

        getLogger().info("RusTerritory 已启用 - 支持 Folia");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.saveAll();
        }
        getLogger().info("RusTerritory 已禁用");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TerritoryStorage getStorage() {
        return storage;
    }

    public SelectionListener getSelectionListener() {
        return selectionListener;
    }
}
