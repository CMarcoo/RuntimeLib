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
    private URLClassLoader runtimePluginClassLoader;

    public void loadRuntimePlugins() {
        boolean fullLog = runtimeLib.getRuntimeLibConfig().getFullDebug();
        Logger logger = runtimeLib.getLogger();
        List<RuntimePlugin> runtimePlugins = new ArrayList<>();
        File pluginsFolder = new File(runtimeLib.getDataFolder().getAbsolutePath() + File.separator + "runtime-plugins");

        if (!pluginsFolder.exists()) {
            if (fullLog) {
                logger.info("The runtime-plugins folder didn't exist, we created one. \n" +
                        "Please, remember to add your Runtime Plugins here, and not in the parent folder!\n" +
                        "Thanks for choosing RuntimeLib!");
            }
            boolean result = pluginsFolder.mkdirs();
            return;
        }

        if (!pluginsFolder.exists()) {
            logger.warning("ERROR: The plugin folder was not found.");
            return;
        }

        File[] jarFiles = pluginsFolder.listFiles();

        if (jarFiles == null) {
            logger.warning("ERROR: No JAR files were found in the plugins directory!");
            return;
        } else if (fullLog) {
            logger.info("Found following JARs to load:" + Arrays.stream(jarFiles).map(File::getName).collect(Collectors.joining(", ")));
        } else if (jarFiles.length == 0) {
            logger.info("No Runtime JARs were found!");
            return;
        }

        List<File> jarFilesList = Arrays.asList(jarFiles);

        if (fullLog) logger.info("Starting JAR files URL validation phase.");

        List<URL> urlsList = jarFilesList.stream().map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.warning("ERROR: List getting URL for " + file.getName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (fullLog) logger.info("Successfully found and validated " + urlsList.size() + " JAR URLs.");

        URL[] urls = new URL[urlsList.size()];
        urls = urlsList.toArray(urls);

        URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        this.runtimePluginClassLoader = classLoader;

        label:
        for (File candidate : jarFilesList) {
            List<String> classNames = new ArrayList<>();
            try {
                ZipInputStream zip = new ZipInputStream(java.nio.file.Files.newInputStream(candidate.toPath()));
                for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        // This ZipEntry represents a class. Now, what class does it represent?
                        String className = entry.getName().replace('/', '.'); // including ".class"
                        if (fullLog)
                            logger.info("Discovered class content \"" + className + "\" of " + candidate.getName());
                        String addClassName = className.substring(0, className.length() - ".class".length());
                        classNames.add(addClassName);
                    }
                }
                zip.close();
            } catch (IOException e) {
                logger.warning("ERROR: problem during jar zip reading phase!");
                logger.warning(e.getLocalizedMessage());
            }

            for (String className : classNames) {
                try {
                    if (fullLog) logger.info("Attempting to load class \"" + className + "\".");
                    Class<?> tempClass = Class.forName(className, true, classLoader);
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

    }

    public void startAll() {
        this.runtimePlugins.forEach(rp -> {
            runtimeLib.getLogger().info("Enabling runtime plugin " + rp.getClass().getSimpleName());
        });
    }

    public void stopAll() {
        this.runtimePlugins.forEach(RuntimePlugin::onDisable);
    }
}
