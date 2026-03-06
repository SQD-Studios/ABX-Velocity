package net.chamosmp.aBXVelocity.cloud.support;

import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.chamosmp.aBXVelocity.cloud.CloudSupport;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Objects;
import java.util.UUID;

public class CloudNetV4Support implements CloudSupport {
    
    @Override
    public void kick(UUID uniqueId, String reason) {
        String result = reason.replace('§', '&');
        MiniMessage miniMessage = MiniMessage.miniMessage();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        result = serializer.serialize(miniMessage.deserialize(result));
        
        PlayerManager playerManager = ServiceRegistry.registry().defaultInstance(PlayerManager.class);
        Objects.requireNonNull(playerManager, "PlayerManager is null in CloudNetV4")
                .playerExecutor(uniqueId)
                .kick(LegacyComponentSerializer.legacySection().deserialize(result));
    }
    
}
