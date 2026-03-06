package net.chamosmp.aBXVelocity.utils;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import java.util.logging.Logger;

@Plugin(id = "abxvelocity", name = "ABX Velocity",
        version = "0.1.0-SNAPSHOT", url = "https://software.chamosmp.net",
        description = "AdvancedBanX for Velocity", authors = {"Chamogelastos"})
public class BstatsMetrics {

    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;

    @Inject
    public BstatsMetrics(
            ProxyServer server,
            Logger logger,
            Metrics.Factory metricsFactory
    ) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // You can find the plugin id of your plugins on
        // the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 29953;
        Metrics metrics = metricsFactory.make(this, pluginId);

        // You can also add custom charts:
        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "value")
        );
    }
}