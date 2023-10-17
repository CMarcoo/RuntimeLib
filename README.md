# RuntimePlugin Library

**Library Name:** RuntimePlugin

**Feature:** Run custom Spigot plugins without importing SpigotAPI

**Most Important Feature:** Be free and exempt from following GPL in your project.

**Supported Java Version:** Java 8 and above

**Supported Spigot Version:** 1.8.8 and above

**License:** This project is under GNU GPL v3.0. It is not an issue to you as importing this library is not necessary for creating custom runtime plugins! :)

The **RuntimePlugin** library is a powerful tool that enables you to run custom Spigot plugins in your Minecraft server without the need to import the SpigotAPI directly. This library provides an easy and flexible way to develop and run your plugins without the constraints of the GNU General Public License (GPL) that typically applies to Spigot-based projects.

## Features

- **Run Custom Spigot Plugins:** The primary feature of the library is the ability to run custom Spigot plugins, just like you would with the SpigotAPI.

- **Freedom from GPL:** By using the **RuntimePlugin** library, you can develop and distribute your custom plugins without being bound by the GPL. This gives you the freedom to create and share your work more easily.

## Compatibility

- **Supported Java Versions:** The library is compatible with Java 8 and above, making it accessible to a wide range of Java developers.

- **Supported Spigot Versions:** It works with Spigot versions starting from 1.8.8 and above, ensuring compatibility with a variety of Minecraft server versions.

## Usage Examples

To demonstrate how the **RuntimePlugin** library can be used, let's consider an example where you create a simple Spigot plugin. You can run your custom plugin using the RuntimeCore API.

### Example: Java Main Class

```java
import top.cmarco.runtimecore.RuntimePlugin;
import top.cmarco.runtimecore.annotations.SpigotVersion;

/**
 * Hello world!
 */
@SpigotVersion(version = "1.20.2") // optional here
public final class App implements RuntimePlugin {

    @Override
    public void onEnable() {
        System.out.println("Hello World SPIGOT server!");
    }

    @Override
    public void onDisable() {
        System.println("Going to sleep!");
    }
}
```
