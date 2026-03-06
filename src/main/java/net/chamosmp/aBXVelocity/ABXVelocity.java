package net.chamosmp.aBXVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "abxvelocity", name = "ABX Velocity", version = BuildConstants.VERSION, description = "Kura Anti Cheat. Built to perform, used in the ChamoSMP", url = "chamosmp.net", authors = {"Chamogelastos"})
public class ABXVelocity {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
