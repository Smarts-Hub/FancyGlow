package dev.smartshub.fancyglow.nms;

import dev.smartshub.fancyglow.glow.GlowState;
import org.bukkit.entity.Player;

public interface NMSHandler {
    void setGlowing(Player player, GlowState state);
    void removeGlowing(Player player);
}
