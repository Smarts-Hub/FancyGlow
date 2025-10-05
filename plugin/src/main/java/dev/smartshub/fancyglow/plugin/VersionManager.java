package dev.smartshub.fancyglow.plugin;

import dev.smartshub.fancyglow.api.nms.NMSHandler;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

public class VersionManager {

    private static final Logger logger = Bukkit.getLogger();

    public static NMSHandler getNMSHandler() {
        String version = getServerVersion();

        if (version == null) {
            logger.severe("Can't determine server version!");
            return null;
        }

        String handlerClass = "dev.smartshub.fancyglow.nms." + version + ".NMSHandler";

        try {
            Class<?> clazz = Class.forName(handlerClass);
            Constructor<?> constructor = clazz.getConstructor();
            return (NMSHandler) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            logger.severe("NMS handler not found for version: " + version);
            logger.severe("Expected class: " + handlerClass);
            return null;
        } catch (Exception e) {
            logger.severe("Error loading NMS handler for " + version + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getServerVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        logger.info("Bukkit version string: " + bukkitVersion);

        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            logger.info("Server package: " + packageName);

            if (packageName.contains(".v1_")) {
                String version = packageName.substring(packageName.lastIndexOf('.') + 1);
                if (!version.equals("craftbukkit")) {
                    logger.info("Detected version from package: " + version);
                    return version;
                }
            }
        } catch (Exception e) {
            logger.warning("Could not get version from package name: " + e.getMessage());
        }

        return parseVersionFromBukkitVersion(bukkitVersion);
    }

    private static String parseVersionFromBukkitVersion(String bukkitVersion) {
        try {
            String clean = bukkitVersion.split("-")[0].trim();
            String[] parts = clean.split("\\.");

            if (parts.length < 2) {
                logger.warning("Invalid version format: " + bukkitVersion);
                return null;
            }

            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            logger.info("Parsed version: " + major + "." + minor + "." + patch);

            if (major == 1 && minor == 21) {
                if (patch >= 5) {
                    return "v1_21_R5";
                } else if (patch >= 1) {
                    return "v1_21_R1";
                } else {
                    return "v1_21_R1";
                }
            } else if (major == 1 && minor == 20) {
                if (patch >= 5) {
                    return "v1_20_R4";
                } else if (patch >= 3) {
                    return "v1_20_R3";
                } else if (patch >= 2) {
                    return "v1_20_R2";
                } else {
                    return "v1_20_R1";
                }
            }

            logger.warning("Unsupported Minecraft version: " + major + "." + minor + "." + patch);
            return null;
        } catch (NumberFormatException e) {
            logger.severe("Error parsing version numbers from: " + bukkitVersion);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSupportedVersion() {
        return getNMSHandler() != null;
    }

    public static void logVersionInfo() {
        logger.info("=== Server Version Information ===");
        logger.info("Bukkit Version: " + Bukkit.getBukkitVersion());
        logger.info("Server Version: " + Bukkit.getVersion());
        logger.info("Server Name: " + Bukkit.getName());

        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            logger.info("Server Package: " + packageName);
        } catch (Exception e) {
            logger.warning("Could not get package name");
        }

        String detectedVersion = getServerVersion();
        logger.info("Detected NMS Version: " + (detectedVersion != null ? detectedVersion : "UNKNOWN"));
        logger.info("================================");
    }
}
