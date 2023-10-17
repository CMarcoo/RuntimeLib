package top.cmarco.runtimelib.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import top.cmarco.runtimelib.RuntimeLib;

@RequiredArgsConstructor
public final class RuntimeLibConfig {

    private final RuntimeLib runtimeLib;
    private FileConfiguration configuration = null;

    public void saveDefaultConfig() {
        runtimeLib.saveDefaultConfig();
        this.configuration = runtimeLib.getConfig();
    }

    public boolean getFullDebug() {
        return configuration.getBoolean("full-debug");
    }
}
