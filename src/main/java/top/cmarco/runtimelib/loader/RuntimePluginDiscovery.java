package top.cmarco.runtimelib.loader;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import top.cmarco.runtimecore.RuntimePlugin;
import top.cmarco.runtimelib.RuntimeLib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RequiredArgsConstructor
public final class RuntimePluginDiscovery {

    private final RuntimeLib runtimeLib;
    private final List<RuntimePlugin> runtimePlugins = new ArrayList<>();

    public void loadRuntimePlugins() {
        List<RuntimePlugin> runtimePlugins = new ArrayList<>();
        File pluginsFolder = runtimeLib.getDataFolder().getParentFile();
        Logger logger = runtimeLib.getLogger();
        if (pluginsFolder == null || !pluginsFolder.exists()) {
            logger.warning("ERROR: The plugin folder was not found.");
            return;
        }
        File[] jarFiles = pluginsFolder.listFiles(f -> Files.getFileExtension(f.getName()).equals(".jar"));
        if (jarFiles == null) {
            logger.warning("ERROR: No JAR files were found in the plugins directory!");
            return;
        }
        List<File> jarFilesList = Arrays.asList(jarFiles);
        List<Plugin> loadedPlugins = Arrays.asList(Bukkit.getPluginManager().getPlugins());
        List<File> filteredJarList = jarFilesList.stream().filter(f -> loadedPlugins.stream().noneMatch(p -> {
            JavaPlugin plugin = (JavaPlugin) p;
            Method getFileMethod = null;
            try {
                getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
                getFileMethod.setAccessible(true);
                File file = (File) getFileMethod.invoke(plugin);
                return file.equals(f);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                logger.warning("ERROR: Something went wrong while using getFile reflection.");
                logger.warning(e.getLocalizedMessage());
            }
            return false;
        })).collect(Collectors.toList());
        List<URL> urlsList = filteredJarList.stream().map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.warning("ERROR: List getting URL of " + file.getName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        URL[] urls = new URL[urlsList.size()];
        urls = urlsList.toArray(urls);

        try (URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
            label:
            for (File candidate : filteredJarList) {
                List<String> classNames = new ArrayList<String>();
                try {
                    ZipInputStream zip = new ZipInputStream(java.nio.file.Files.newInputStream(candidate.toPath()));
                    for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            // This ZipEntry represents a class. Now, what class does it represent?
                            final String className = entry.getName().replace('/', '.'); // including ".class"
                            classNames.add(className.substring(0, className.length() - ".class".length()));
                        }
                    }
                    zip.close();
                } catch (IOException e) {
                    logger.warning("ERROR: problem during jar zip reading phase!");
                    logger.warning(e.getLocalizedMessage());
                }

                for (String className : classNames) {
                    try {
                        Class<?> tempClass = Class.forName(className, Boolean.TRUE, classLoader);
                        if (RuntimePlugin.class.isAssignableFrom(tempClass)) {
                            logger.info("Found suitable plugin candidate class: " + className);
                            logger.info("Attempting to load . . .");
                            // assume safe constructor cast
                            Constructor<? extends RuntimePlugin> tempConstructor = (Constructor<? extends RuntimePlugin>) tempClass.getConstructor();
                            RuntimePlugin instance = tempConstructor.newInstance();
                            this.runtimePlugins.add(instance);
                            continue label;
                        }
                    } catch (ClassNotFoundException e) {
                        logger.warning("ERROR: problem during class forName search phase!");
                        logger.warning(e.getLocalizedMessage());
                    } catch (NoSuchMethodException e) {
                        logger.warning("ERROR: could not find standard constructor for " + className);
                        logger.warning(e.getLocalizedMessage());
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        logger.warning("ERROR: Could not create new instance for " + className);
                        logger.warning(e.getLocalizedMessage());
                    }
                }
            }
        } catch (IOException exception) {
            logger.warning("ERROR: Could not build\\get system classloader.");
        }
    }

    public void startAll() {
        this.runtimePlugins.forEach(RuntimePlugin::onEnable);
    }

    public void stopAll() {
        this.runtimePlugins.forEach(RuntimePlugin::onDisable);
    }
}
