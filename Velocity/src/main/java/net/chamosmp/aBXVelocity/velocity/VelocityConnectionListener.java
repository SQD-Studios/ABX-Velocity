package net.chamosmp.aBXVelocity.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import net.hnt8.advancedban.utils.Universal;
import net.hnt8.advancedban.utils.manager.PunishmentManager;
import net.hnt8.advancedban.utils.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityConnectionListener {

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        return EventTask.async(() -> {
            Player player = event.getPlayer();
            UUIDManager.get().supplyInternUUID(player.getUsername(), player.getUniqueId());
            String ip = player.getRemoteAddress() != null ? player.getRemoteAddress().getAddress().getHostAddress() : null;

            String result = Universal.get().callConnection(player.getUsername(), ip);
            if (result != null) {
                Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(result.replace('§', '&'));
                event.setResult(ResultedEvent.ComponentResult.denied(comp));
            }
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (event.getPlayer() != null) {
            PunishmentManager.get().discard(event.getPlayer().getUsername());
        }
    }
}
