package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.api.glow.GlowState;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.registry.GlowStateRegistry;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class GlowHandlingService {

    private final GlowEffectService glowEffectService;
    private final GlowPersistenceService glowPersistenceService;
    private final GlowPolicyService glowPolicyService;
    private final GlowModeRegistry glowModeRegistry;
    private final GlowStateRegistry glowStateRegistry;
    private final NotifyService notifyService;

    public GlowHandlingService(GlowEffectService glowEffectService,
                               GlowPersistenceService glowPersistenceService,
                               GlowPolicyService glowPolicyService,
                               GlowModeRegistry glowModeRegistry,
                               GlowStateRegistry glowStateRegistry,
                               NotifyService notifyService) {
        this.notifyService = notifyService;
        this.glowEffectService = glowEffectService;
        this.glowPersistenceService = glowPersistenceService;
        this.glowPolicyService = glowPolicyService;
        this.glowModeRegistry = glowModeRegistry;
        this.glowStateRegistry = glowStateRegistry;
    }

    public void playerJoin(Player player) {
        if (glowPolicyService.isWorldDisabled(player)) return;
        if (!glowPolicyService.isPersistent()) return;

        glowPersistenceService.getStoredMode(player.getUniqueId())
                .thenAccept(optionalModeId -> {
                    if (optionalModeId.isEmpty()) return;

                    GlowMode mode = glowModeRegistry.getGlowModeById(optionalModeId.get());
                    if (mode == null) return;

                    set(player, mode);
                });
    }

    public void playerQuit(Player player) {
        GlowState state = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (state == null) return;
        glowStateRegistry.unregister(state);
    }

    public void color(Player player, GlowMode glowMode) {
        if (glowPolicyService.isWorldDisabled(player)) {
            notifyService.sendChat(player, "disabled-world");
            return;
        }

        long currentTick = player.getWorld().getFullTime();
        GlowState state = new GlowState(player.getUniqueId(), glowMode, currentTick);

        glowEffectService.applyGlowEffect(player, state);
        glowStateRegistry.register(state);

        if (!glowPolicyService.isPersistent()) return;

        glowPersistenceService.saveMode(player.getUniqueId(), glowMode.getId())
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }


    public void toggle(Player player) {
        System.out.println("Entered in toggle method for player: " + player.getName());
        GlowState currentState = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());

        if (currentState != null) {
            System.out.println("Toggling off glow for player: " + player.getName());
            System.out.println("Current State is not null: " + currentState.getCurrentColor() + " " + currentState.getMode().getId());
            System.out.println("Calling off method (but keeping persistence).");

            glowEffectService.removeGlowEffect(player);
            glowStateRegistry.unregister(currentState);
            return;
        }

        glowPersistenceService.getStoredMode(player.getUniqueId())
                .thenAccept(optionalModeId -> {
                    System.out.println("Optional Mode ID: " + optionalModeId);
                    if (optionalModeId.isEmpty()) return;

                    GlowMode mode = glowModeRegistry.getGlowModeById(optionalModeId.get());
                    System.out.println("Retrieved Mode: " + (mode != null ? mode.getId() : "null"));
                    if (mode == null) return;

                    color(player, mode);
                    System.out.println("Applied glow mode: " + mode.getId() + " to player: " + player.getName());
                });
    }


    public void off(Player player) {
        glowEffectService.removeGlowEffect(player);

        GlowState state = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (state != null) {
            glowStateRegistry.unregister(state);
        }

        if (glowPolicyService.isPersistent()) {
            glowPersistenceService.removeMode(player.getUniqueId());
        }
    }

    public void offOthers(Player toDisable) {
        off(toDisable);
    }

    public void offAll() {
        Bukkit.getOnlinePlayers().forEach(this::off);
    }

    public void set(Player toSet, GlowMode glowMode) {
        color(toSet, glowMode);
        notifyService.sendChat(toSet, "glow-set");
    }
}
