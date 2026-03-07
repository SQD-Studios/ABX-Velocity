package net.hnt8.advancedban.bukkit.utils;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;

public class BukkitMetrics {

    private final Metrics metrics;

    public BukkitMetrics(JavaPlugin plugin, int pluginId) {
        this.metrics = new Metrics(plugin, pluginId);
    }

    public void addCustomChart(CustomChart chart) {
        metrics.addCustomChart(chart);
    }

    public static class SimplePie extends org.bstats.charts.SimplePie {
        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId, callable);
        }
    }
}
