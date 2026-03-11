package net.hnt8.advancedban.bukkit.paper;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.command.brigadier.*;
import com.mojang.brigadier.*;

public class Bootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register your commands here ...
            LiteralArgumentBuilder<CommandSourceStack> advancedban = Commands.literal("advancedban")
                    .then(Commands.literal("help"))
                    .then(Commands.literal("reload"));
            LiteralArgumentBuilder<CommandSourceStack> systemprefs = Commands.literal("systemprefs");

        });


    }
}