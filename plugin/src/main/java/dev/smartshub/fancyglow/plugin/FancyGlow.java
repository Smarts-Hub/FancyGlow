package dev.smartshub.fancyglow.plugin;

import dev.smartshub.fancyglow.plugin.command.GlowCommand;
import dev.smartshub.fancyglow.plugin.command.exception.ExceptionHandler;
import dev.smartshub.fancyglow.plugin.command.parameter.GlowModeParameter;
import dev.smartshub.fancyglow.plugin.command.parameter.PlayerParameter;
import dev.smartshub.fancyglow.api.config.ConfigType;
import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.hook.bstats.Metrics;
import dev.smartshub.fancyglow.plugin.hook.placeholderapi.PlaceholderAPIHook;
import dev.smartshub.fancyglow.plugin.message.MessageParser;
import dev.smartshub.fancyglow.plugin.message.MessageRepository;
import dev.smartshub.fancyglow.api.nms.NMSHandler;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.listener.PlayerChangeWorldListener;
import dev.smartshub.fancyglow.plugin.listener.PlayerJoinListener;
import dev.smartshub.fancyglow.plugin.listener.PlayerQuitListener;
import dev.smartshub.fancyglow.plugin.registry.GlowStateRegistry;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;
import dev.smartshub.fancyglow.plugin.service.flow.LifecycleService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowEffectService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowPersistenceService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowPolicyService;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import dev.smartshub.fancyglow.plugin.storage.database.connection.DatabaseConnection;
import dev.smartshub.fancyglow.plugin.storage.database.dao.PlayerGlowDAO;
import dev.smartshub.fancyglow.plugin.storage.database.table.SchemaCreator;
import dev.smartshub.fancyglow.plugin.task.AsyncJobTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import revxrsal.commands.bukkit.BukkitLamp;

import java.util.concurrent.CompletableFuture;

public class FancyGlow extends JavaPlugin {

    private MessageRepository messageRepository;
    private MessageParser messageParser;
    private ConfigService configService;
    private GlowModeRegistry glowModeRegistry;
    private GlowStateRegistry glowStateRegistry;
    private NMSHandler nmsHandler;
    private GlowEffectService glowEffectService;
    private GlowPersistenceService glowPersistenceService;
    private GlowPolicyService glowPolicyService;
    private GlowHandlingService glowHandlingService;
    private LifecycleService lifecycleService;
    private NotifyService notifyService;

    @Override
    public void onEnable() {
        getLogger().info("FancyGlow enabled!");
        setUpConfig();
        setUpStorage();
        initRegistries();
        initNMSHandler();
        initServices();
        startTicking();
        registerListeners();
        registerCommands();
        hook();
    }

    @Override
    public void onDisable() {
        getLogger().info("FancyGlow disabled!");
    }

    private void setUpConfig() {
        messageParser = new MessageParser();
        configService = new ConfigService(this);
        messageRepository = new MessageRepository(configService);
    }

    private void setUpStorage() {
        CompletableFuture.runAsync(() -> {
            DatabaseConnection.init(configService.provide(ConfigType.DATABASE));
            SchemaCreator.createSchema();
        });
    }

    private void initRegistries() {
        glowModeRegistry = new GlowModeRegistry(configService);
        glowStateRegistry = new GlowStateRegistry();
    }

    private void initNMSHandler() {
        this.nmsHandler = VersionManager.getNMSHandler();
    }

    private void initServices() {
        notifyService = new NotifyService(messageParser, messageRepository);

        glowEffectService = new GlowEffectService(nmsHandler, notifyService);
        glowPersistenceService = new GlowPersistenceService(new PlayerGlowDAO());
        glowPolicyService = new GlowPolicyService(configService, notifyService);
        glowHandlingService = new GlowHandlingService(glowEffectService, glowPersistenceService, glowPolicyService,
                glowModeRegistry, glowStateRegistry, notifyService);

        lifecycleService = new LifecycleService(configService, glowModeRegistry);
    }

    private void startTicking() {
        lifecycleService.setAsyncJobTask(new AsyncJobTask(this, nmsHandler, glowStateRegistry, glowModeRegistry));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(glowHandlingService), this);
        pm.registerEvents(new PlayerQuitListener(glowHandlingService), this);
        pm.registerEvents(new PlayerChangeWorldListener(glowHandlingService, glowPolicyService), this);
    }

    private void registerCommands() {

        final var exceptionHandler = new ExceptionHandler(notifyService);

        var lamp = BukkitLamp.builder(this)
                .parameterTypes(builder -> {
                    builder.addParameterType(GlowMode.class, new GlowModeParameter(glowModeRegistry, notifyService));
                    builder.addParameterType(Player.class, new PlayerParameter(notifyService));
                })
                .exceptionHandler(exceptionHandler)
                .build();

        lamp.register(new GlowCommand(glowHandlingService, lifecycleService, notifyService));
    }

    private void hook() {
        // BStats
        new Metrics(this, 22057);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("Could not find PlaceholderAPI!");
            return;
        }
        new PlaceholderAPIHook(glowStateRegistry);
    }

}
