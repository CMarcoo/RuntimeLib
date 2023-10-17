package top.cmarco.runtimelib;

import org.bukkit.plugin.java.JavaPlugin;
import top.cmarco.runtimelib.loader.RuntimePluginDiscovery;

public final class RuntimeLib extends JavaPlugin {

    private RuntimePluginDiscovery pluginDiscovery = null;

    public void startDiscoveryProcess() {
        pluginDiscovery = new RuntimePluginDiscovery(this);
        getLogger().info("Starting to load all RuntimePlugins . . .");
        long then = System.currentTimeMillis();
        pluginDiscovery.loadRuntimePlugins();
        pluginDiscovery.startAll();
        long now = System.currentTimeMillis();
        getLogger().info(String.format("Operation finished in %.3fs", (now-then) / 1000f));
    }

    public void stopDiscoveryProcess() {
        getLogger().info("Killing all RuntimePlugins . . .");
        pluginDiscovery.stopAll();
    }

    @Override
    public void onEnable() { // Plugin startup logic
        startDiscoveryProcess();
    }

    @Override
    public void onDisable() { // Plugin shutdown logic
        stopDiscoveryProcess();
    }
}
