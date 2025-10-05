package dev.smartshub.fancyglow.api.nms;

import dev.smartshub.fancyglow.api.glow.GlowState;
import org.bukkit.entity.Player;

public interface NMSHandler {
    void setGlowing(Player player, GlowState state);
    void removeGlowing(Player player);
}
