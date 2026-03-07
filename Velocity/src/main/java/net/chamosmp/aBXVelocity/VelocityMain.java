package net.chamosmp.aBXVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.chamosmp.aBXVelocity.cloud.CloudSupport;
import net.chamosmp.aBXVelocity.cloud.CloudSupportHandler;
import net.chamosmp.aBXVelocity.velocity.VelocityChatListener;
import net.chamosmp.aBXVelocity.velocity.VelocityConnectionListener;
import net.chamosmp.aBXVelocity.velocity.VelocityPluginMessageListener;
import net.hnt8.advancedban.utils.Universal;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "abxvelocity",
        name = "ABX Velocity",
        version = "1.2",
        description = "AdvancedBanX for Velocity",
        authors = {"Chamogelastos"}
)
public class VelocityMain {
    private static VelocityMain instance;

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;
    private final ChannelIdentifier mainChannel = MinecraftChannelIdentifier.create("advancedban", "main");
    private final ChannelIdentifier connectionChannel = MinecraftChannelIdentifier.create("advancedban", "connection");

    private CloudSupport cloudSupport;

    @Inject
    public VelocityMain(ProxyServer proxy,
                        Logger logger,
                        @DataDirectory Path dataDirectory,
                        Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    public static VelocityMain get() {
        return instance;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Metrics.Factory getMetricsFactory() {
        return metricsFactory;
    }

    public ChannelIdentifier getMainChannel() {
        return mainChannel;
    }

    public ChannelIdentifier getConnectionChannel() {
        return connectionChannel;
    }

    public CloudSupport getCloudSupport() {
        return cloudSupport;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        proxy.getChannelRegistrar().register(mainChannel, connectionChannel);
        cloudSupport = CloudSupportHandler.getCloudSystem();

        Universal.get().setup(new VelocityMethods(this));

        proxy.getEventManager().register(this, new VelocityChatListener());
        proxy.getEventManager().register(this, new VelocityConnectionListener());
        proxy.getEventManager().register(this, new VelocityPluginMessageListener());
        // You can find the plugin id of your plugins on
        // the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 29953;
        Metrics metrics = metricsFactory.make(this, pluginId);

        // You can also add custom charts:
        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "value")
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        Universal.get().shutdown();
    }

}
