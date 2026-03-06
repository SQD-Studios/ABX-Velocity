package net.chamosmp.aBXVelocity.cloud;

import net.chamosmp.aBXVelocity.cloud.support.CloudNetV4Support;
import net.md_5.bungee.api.ProxyServer;

public class CloudSupportHandler {

    public static CloudSupport getCloudSystem() {
        try {
            if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-Bridge") != null) {
                Class.forName("eu.cloudnetservice.modules.bridge.player.PlayerManager");
                return new CloudNetV4Support();
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
