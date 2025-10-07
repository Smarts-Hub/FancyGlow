package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.api.glow.GlowState;
import dev.smartshub.fancyglow.api.nms.NMSHandler;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.bukkit.entity.Player;

public class GlowEffectService {

    private final NMSHandler nmsHandler;
    private final NotifyService notifyService;

    public GlowEffectService(NMSHandler nmsHandler, NotifyService notifyService) {
        this.nmsHandler = nmsHandler;
        this.notifyService = notifyService;
    }

    public void applyGlowEffect(Player player, GlowState state) {
        notifyService.sendChat(player, "glow-enabled");
        nmsHandler.setGlowing(player, state);
    }

    public void removeGlowEffect(Player player) {
        notifyService.sendChat(player, "glow-disabled");
        nmsHandler.removeGlowing(player);
    }
}
