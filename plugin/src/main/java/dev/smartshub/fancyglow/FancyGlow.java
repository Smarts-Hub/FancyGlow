package dev.smartshub.fancyglow;

import dev.smartshub.fancyglow.message.MessageParser;
import dev.smartshub.fancyglow.message.MessageRepository;
import dev.smartshub.fancyglow.nms.NMSHandler;
import dev.smartshub.fancyglow.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.registry.GlowStateRegistry;
import dev.smartshub.fancyglow.service.config.ConfigService;
import dev.smartshub.fancyglow.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.service.notify.NotifyService;
import dev.smartshub.fancyglow.storage.database.connection.DatabaseConnection;
import dev.smartshub.fancyglow.storage.database.dao.PlayerGlowDAO;
import dev.smartshub.fancyglow.storage.database.table.SchemaCreator;
import dev.smartshub.fancyglow.task.AsyncJobTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class FancyGlow extends JavaPlugin {

    private ConfigService configService;
    private GlowModeRegistry glowModeRegistry;
    private GlowStateRegistry glowStateRegistry;
    private NMSHandler nmsHandler;
    private GlowHandlingService glowHandlingService;
    private NotifyService notifyService;
    private AsyncJobTask asyncJobTask;

    @Override
    public void onEnable() {
        // Initialize configs
        this.configService = new ConfigService(this);

        // Initialize database and schema
        DatabaseConnection.init(configService.provide(dev.smartshub.fancyglow.config.ConfigType.DATABASE));
        SchemaCreator.createSchema();

        // Initialize registries and NMS
        this.glowModeRegistry = new GlowModeRegistry(configService);
        this.glowStateRegistry = new GlowStateRegistry();
        this.nmsHandler = VersionManager.getNMSHandler();
        if (nmsHandler == null) {
            getLogger().severe("Unsupported server version or NMS handler missing. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Messaging & notify service
        MessageParser parser = new MessageParser();
        MessageRepository messageRepository = new MessageRepository(configService);
        this.notifyService = new NotifyService(parser, messageRepository);

        // Glow handling service
        PlayerGlowDAO dao = new PlayerGlowDAO();
        this.glowHandlingService = new GlowHandlingService(nmsHandler, glowStateRegistry, glowModeRegistry, dao, configService, notifyService);

        // Register listeners
        registerListeners();


        // Start async job task at GCD period
        this.asyncJobTask = new AsyncJobTask(this, nmsHandler, glowStateRegistry, glowModeRegistry);
        this.asyncJobTask.start();

        getLogger().info("FancyGlow enabled!");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new dev.smartshub.fancyglow.listener.PlayerJoinListener(glowHandlingService), this);
        pm.registerEvents(new dev.smartshub.fancyglow.listener.PlayerQuitListener(glowHandlingService), this);
        pm.registerEvents(new dev.smartshub.fancyglow.listener.PlayerChangeWorldListener(glowHandlingService), this);
    }

    @Override
    public void onDisable() {
        // Cancel task
        if (asyncJobTask != null) {
            try { asyncJobTask.cancel(); } catch (Exception ignored) {}
        }
        // Remove visual glow from all online players and persist states per settings
        Bukkit.getOnlinePlayers().forEach(glowHandlingService::handleLeave);
        getLogger().info("FancyGlow disabled!");
    }


    public void restartAsyncTask() {
        if (asyncJobTask != null) {
            try { asyncJobTask.cancel(); } catch (Exception ignored) {}
        }
        this.asyncJobTask = new AsyncJobTask(this, nmsHandler, glowStateRegistry, glowModeRegistry);
        this.asyncJobTask.start();
        getLogger().info("[FancyGlow] Async job task restarted");
    }
}
