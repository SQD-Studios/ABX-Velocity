package net.chamosmp.aBXVelocity.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.hnt8.advancedban.Universal;
import net.hnt8.advancedban.manager.CommandManager;

import java.util.List;

public class VelocitySimpleCommand implements SimpleCommand {
    private final String name;
    private final String permission;
    private final net.hnt8.advancedban.utils.tabcompletion.TabCompleter tabCompleter;

    public VelocitySimpleCommand(String name, String permission, net.hnt8.advancedban.utils.tabcompletion.TabCompleter tabCompleter) {
        this.name = name;
        this.permission = permission;
        this.tabCompleter = tabCompleter;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandManager.get().onCommand(invocation.source(), name, invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (tabCompleter == null) {
            return List.of();
        }
        return tabCompleter.onTabComplete(invocation.source(), invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (permission == null) {
            return true;
        }
        if (invocation.source() instanceof Player player) {
            return Universal.get().hasPerms(player, permission);
        }
        return Universal.get().hasPerms(invocation.source(), permission);
    }
}
