package net.hnt8.advancedban.utils.util;

import com.mojang.brigadier.Command;
import com.sun.source.util.Plugin;
import com.mojang.brigadier.*;
import io.papermc.paper.command.brigadier.Commands;

import static net.hnt8.advancedban.utils.util.Punishment.mi;

public class BrigadierCommands {
    public static void register(Commands commands) {
        commands.register(Commands.literal("advancedbanx")
                        .requires(sender -> sender.getSender().hasPermission("advancedbanx.*"))
                        .executes(ctx ->{
                            ctx.getSource().getSender().sendMessage("ABX Velocity. Do /advancedbanx help for more info!");
                        })
                .then(Commands.literal("help"))
                        .requires(sender -> sender.getSender().hasPermission("advancedbanx.help"))
                        .executes(ctx ->{
                            ctx.getSource().getSender().sendPlainMessage("");
                        })
                .then(Commands.literal("reload"))
                .requires(sender -> sender.getSender().hasPermission("advancedban.reload"))
                .executes(ctx ->{
                    mi.loadFiles();
                })




        );

    }
}
