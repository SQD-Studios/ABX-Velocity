package net.chamosmp.aBXVelocity.cloud;

import net.chamosmp.aBXVelocity.cloud.support.CloudNetV4Support;

public class CloudSupportHandler {

    public static CloudSupport getCloudSystem() {
        try {
            Class.forName("eu.cloudnetservice.modules.bridge.player.PlayerManager");
            return new CloudNetV4Support();
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
