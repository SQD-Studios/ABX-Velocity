package net.chamosmp.aBXVelocity.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import net.hnt8.advancedban.utils.Command;
import net.hnt8.advancedban.Universal;

public class VelocityChatListener {

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (Universal.get().getMethods().callChat(event.getPlayer())) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (event.getCommandSource() == null) {
            return;
        }
        if (Universal.get().getMethods().callCMD(event.getCommandSource(), "/" + event.getCommand())) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        final String commandName = event.getPartialMessage().split(" ")[0];
        if (commandName.length() > 1 && event.getPartialMessage().length() > commandName.length()) {
            final Command command = Command.getByName(commandName.substring(1));
            if (command != null) {
                if (command.getPermission() == null || Universal.get().getMethods().hasPerms(event.getPlayer(), command.getPermission())) {
                    final String[] args = event.getPartialMessage().substring(commandName.length() + 1).split(" ", -1);
                    event.getSuggestions().addAll(command.getTabCompleter().onTabComplete(event.getPlayer(), args));
                }
            }
        }
    }
}
