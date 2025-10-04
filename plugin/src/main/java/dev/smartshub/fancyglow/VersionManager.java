package dev.smartshub.fancyglow;

import dev.smartshub.fancyglow.nms.NMSHandler;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

public class VersionManager {
    
    private static final Logger logger = Bukkit.getLogger();

    public static NMSHandler getNMSHandler() {
        String version = getServerVersion();
        
        if (version == null) {
            logger.severe("Can't determinate server version!");
            return null;
        }
        
        String handlerClass = "dev.smartshub.fancyglow.NMSHandler";
        
        try {
            Class<?> clazz = Class.forName(handlerClass);
            Constructor<?> constructor = clazz.getConstructor();
            return (NMSHandler) constructor.newInstance();
            
        } catch (ClassNotFoundException e) {
            logger.severe("NMS not found for version: " + version);
            return null;
        } catch (Exception e) {
            logger.severe("Issues loading handler NMS for " + version + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getServerVersion() {
        try {
            String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName();
            return bukkitVersion.substring(bukkitVersion.lastIndexOf('.') + 1);
        } catch (Exception e) {
            logger.warning("Can't obtain natively, trying alternative way...");
            
            try {
                String version = Bukkit.getBukkitVersion();
                if (version.contains("1.21.5")|| version.contains("1.21.6")|| version.contains("1.21.7") || version.contains("1.21.8")) {
                    return "v1_21_R5";
                } else if (version.contains("1.20.0")) {
                    return "v1_20_R1";
                }
                
                logger.warning("Version not supported: " + version);
                return null;
                
            } catch (Exception ex) {
                logger.severe("Error getting server version: " + ex.getMessage());
                return null;
            }
        }
    }

    public static boolean isSupportedVersion() {
        return getNMSHandler() != null;
    }

}