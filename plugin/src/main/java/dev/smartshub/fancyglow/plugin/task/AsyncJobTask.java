package dev.smartshub.fancyglow.plugin.task;

import dev.smartshub.fancyglow.api.glow.GlowState;
import dev.smartshub.fancyglow.api.nms.NMSHandler;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.registry.GlowStateRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncJobTask extends BukkitRunnable {

    private final Plugin plugin;
    private final NMSHandler nmsHandler;
    private final GlowStateRegistry glowStateRegistry;

    private final int periodTicks;

    // Steps remaining until next update
    private final Map<UUID, Integer> stepsRemaining = new ConcurrentHashMap<>();
    // Steps per update (ticksPerColor / periodTicks)
    private final Map<UUID, Integer> stepsPerUpdate = new ConcurrentHashMap<>();

    public AsyncJobTask(Plugin plugin,
                        NMSHandler nmsHandler,
                        GlowStateRegistry glowStateRegistry,
                        GlowModeRegistry glowModeRegistry) {
        this.plugin = plugin;
        this.nmsHandler = nmsHandler;
        this.glowStateRegistry = glowStateRegistry;
        this.periodTicks = Math.max(1, TaskIntervalResolver.resolvePeriodTicks(glowModeRegistry));
        start();
    }

    public void start() {
        this.runTaskTimerAsynchronously(plugin, periodTicks, periodTicks);
    }

    @Override
    public void run() {
        Set<GlowState> states = glowStateRegistry.getAllGlows();

        cleanupCaches(states);

        long currentTick = System.currentTimeMillis() / 50L;

        for (GlowState state : states) {
            UUID id = state.getPlayerId();

            int spu = computeStepsPerUpdate(state);
            int remaining = stepsRemaining.compute(id, (k, v) -> {
                Integer lastSpu = stepsPerUpdate.get(k);
                if (v == null || lastSpu == null || lastSpu != spu) {
                    stepsPerUpdate.put(k, spu);
                    return spu; // reset
                }
                return Math.max(1, v - 1);
            });

            if (remaining <= 1) {
                state.updateColor(currentTick);
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline()) {
                    nmsHandler.setGlowing(player, state);
                }
                stepsRemaining.put(id, spu);
            }
        }
    }

    private int computeStepsPerUpdate(GlowState state) {
        long tpc = state.getMode().getTicksPerColor();
        int spu = (int) Math.max(1, tpc / periodTicks);
        return spu;
    }

    private void cleanupCaches(Set<GlowState> states) {
        Set<UUID> active = ConcurrentHashMap.newKeySet();
        for (GlowState s : states) {
            active.add(s.getPlayerId());
        }
        stepsRemaining.keySet().removeIf(id -> !active.contains(id));
        stepsPerUpdate.keySet().removeIf(id -> !active.contains(id));
    }
}
