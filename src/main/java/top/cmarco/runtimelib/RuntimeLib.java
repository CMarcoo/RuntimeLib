package top.cmarco.runtimelib;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import top.cmarco.runtimelib.config.RuntimeLibConfig;
import top.cmarco.runtimelib.loader.RuntimePluginDiscovery;

/**
 * The `RuntimeLib` class is a Minecraft plugin that manages the discovery, loading, and operation
 * of runtime plugins within the server. It extends the `JavaPlugin` class and provides methods to
 * load and manage runtime plugins, along with loading and managing its own configuration.
 *
 * @author Marco C.
 * @version 1.0.0
 * @since 15 Oct. 2023
 */
public final class RuntimeLib extends JavaPlugin {

    private RuntimePluginDiscovery pluginDiscovery = null;
    @Getter private RuntimeLibConfig runtimeLibConfig;

    /**
     * Load the configuration for the `RuntimeLib` plugin. If the configuration file does not exist,
     * a default configuration will be saved.
     */
    private void loadConfig() {
        runtimeLibConfig = new RuntimeLibConfig(this);
        runtimeLibConfig.saveDefaultConfig();
    }

    /**
     * Start the discovery process to load all runtime plugins. This method initializes the
     * `RuntimePluginDiscovery` and loads the plugins, then starts them.
     */
    private void startDiscoveryProcess() {
        pluginDiscovery = new RuntimePluginDiscovery(this);
        getLogger().info("Starting to load all RuntimePlugins . . .");
        long then = System.currentTimeMillis();
        pluginDiscovery.loadRuntimePlugins();
        pluginDiscovery.startAll();
        long now = System.currentTimeMillis();
        getLogger().info(String.format("Operation finished in %.3fs", (now-then) / 1000f));
    }

    /**
     * Stop the discovery process and disable all loaded runtime plugins.
     */
    private void stopDiscoveryProcess() {
        getLogger().info("Killing all RuntimePlugins . . .");
        pluginDiscovery.stopAll();
    }

    /**
     * Called when the plugin is enabled. This method initializes the plugin by loading its configuration
     * and starting the discovery process to load and enable runtime plugins.
     */
    @Override
    public void onEnable() { // Plugin startup logic
        loadConfig();
        startDiscoveryProcess();
    }

    /**
     * Called when the plugin is disabled. This method stops the discovery process and disables all
     * loaded runtime plugins.
     */
    @Override
    public void onDisable() { // Plugin shutdown logic
        stopDiscoveryProcess();
    }
}
