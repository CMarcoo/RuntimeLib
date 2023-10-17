package top.cmarco.runtimelib;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import top.cmarco.runtimelib.config.RuntimeLibConfig;
import top.cmarco.runtimelib.loader.RuntimePluginDiscovery;

public final class RuntimeLib extends JavaPlugin {

    private RuntimePluginDiscovery pluginDiscovery = null;
    @Getter private RuntimeLibConfig runtimeLibConfig;

    private void loadConfig() {
        runtimeLibConfig = new RuntimeLibConfig(this);
        runtimeLibConfig.saveDefaultConfig();
    }

    private void startDiscoveryProcess() {
        pluginDiscovery = new RuntimePluginDiscovery(this);
        getLogger().info("Starting to load all RuntimePlugins . . .");
        long then = System.currentTimeMillis();
        pluginDiscovery.loadRuntimePlugins();
        pluginDiscovery.startAll();
        long now = System.currentTimeMillis();
        getLogger().info(String.format("Operation finished in %.3fs", (now-then) / 1000f));
    }

    private void stopDiscoveryProcess() {
        getLogger().info("Killing all RuntimePlugins . . .");
        pluginDiscovery.stopAll();
    }

    @Override
    public void onEnable() { // Plugin startup logic
        loadConfig();
        startDiscoveryProcess();
    }

    @Override
    public void onDisable() { // Plugin shutdown logic
        stopDiscoveryProcess();
    }
}
