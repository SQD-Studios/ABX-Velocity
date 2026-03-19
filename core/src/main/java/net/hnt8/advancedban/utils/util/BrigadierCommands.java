package net.hnt8.advancedban.utils.util;

import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.hnt8.advancedban.utils.MethodInterface;
import com.mojang.brigadier.*;
import io.papermc.paper.command.brigadier.Commands;
import net.hnt8.advancedban.utils.Universal;
import net.hnt8.advancedban.utils.manager.DatabaseManager;
import net.hnt8.advancedban.utils.manager.MessageManager;
import net.hnt8.advancedban.utils.manager.PunishmentManager;
import net.hnt8.advancedban.utils.manager.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.annotation.Target;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static net.hnt8.advancedban.utils.util.CommandUtils.processName;
import static net.hnt8.advancedban.utils.util.Punishment.mi;

public class BrigadierCommands {
    public static void register(Commands commands) {
        commands.register(Commands.literal("advancedbanx")
                        .requires(sender -> sender.getSender().hasPermission("advancedbanx.*"))
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            mi.sendMessage(sender, "<bold><dark_gray><strikethrough>-=====</strikethrough></dark_gray> <#00ffe2>AdvancedBanX v3</#00ffe2> <dark_gray><strikethrough>=====-</strikethrough></dark_gray></bold>");
                            mi.sendMessage(sender, "  <#00ffe2>Dev</#00ffe2> <dark_gray>•</dark_gray> <gray>Leoko</gray>");
                            mi.sendMessage(sender, "  <#00ffe2>Maintainer</#00ffe2> <dark_gray>•</dark_gray> <gray>2vY (hlpdev)</gray>");
                            mi.sendMessage(sender, "  <#00ffe2>Status</#00ffe2> <dark_gray>•</dark_gray> <green><italic>Stable</italic></green>");
                            mi.sendMessage(sender, "  <#00ffe2>Version</#00ffe2> <dark_gray>•</dark_gray> <gray>" + mi.getVersion() + "</gray>");
                            mi.sendMessage(sender, "  <#00ffe2>License</#00ffe2> <dark_gray>•</dark_gray> <gray>Public</gray>");
                            mi.sendMessage(sender, "  <#00ffe2>Storage</#00ffe2> <dark_gray>•</dark_gray> <gray>" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)</gray>" : "HSQLDB (local)</gray>"));
                            mi.sendMessage(sender, "  <#00ffe2>Server</#00ffe2> <dark_gray>•</dark_gray> <gray>" + (Universal.get().isBungee() ? "Bungeecord</gray>" : "Bukkit/Spigot/Paper</gray>"));
                            if (Universal.get().isBungee()) {
                                mi.sendMessage(sender, "  <#00ffe2>RedisBungee</#00ffe2> <dark_gray>•</dark_gray> <gray>" + (Universal.isRedis() ? "true</gray>" : "false</gray>"));
                            }
                            mi.sendMessage(sender, "  <#00ffe2>UUID-Mode</#00ffe2> <dark_gray>•</dark_gray> <gray>" + UUIDManager.get().getMode() + "</gray>");
                            mi.sendMessage(sender, "  <#00ffe2>Prefix</#00ffe2> <dark_gray>•</dark_gray> <gray>" + (mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "</gray>" : MessageManager.getMessage("General.Prefix") + "</gray>"));
                            mi.sendMessage(sender, "<bold><dark_gray><strikethrough>-=========================-</strikethrough></dark_gray></bold>");
                            return 1;
                        })
                        .then(Commands.literal("help"))
                        .requires(sender -> sender.getSender().hasPermission("advancedbanx.help"))
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            mi.sendMessage(sender, "");
                            mi.sendMessage(sender, "<#00ffe2><bold>AdvancedBanX</bold></#00ffe2> <gray>Command-Help</gray>");
                            mi.sendMessage(sender, "");
                            mi.sendMessage(sender, "<#00ffe2>/ban [Name] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Ban a user permanently</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/banip [Name/IP] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Ban a user by IP</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/tempban [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Ban a user temporary</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/mute [Name] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Mute a user permanently</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/tempmute [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Mute a user temporary</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/warn [Name] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Warn a user permanently</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/note [Name] [Note]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Adds a note to a user</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/tempwarn [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Warn a user temporary</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/kick [Name] [Reason/@Layout]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Kick a user</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/unban [Name/IP]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Unban a user</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/unmute [Name]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Unmute a user</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/unwarn [ID] or /unwarn clear [Name]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Deletes a warn</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/unnote [ID] or /unnote clear [Name]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Deletes a note</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/change-reason [ID or ban/mute USER] [New reason]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Changes the reason of a punishment</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/unpunish [ID]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Deletes a punishment by ID</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/banlist <Page></#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>See all punishments</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/history [Name/IP] <Page></#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>See a users history</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/warns [Name] <Page></#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>See your or a users warnings</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/notes [Name] <Page></#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>See your or a users notes</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/check [Name]</#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Get all information about a user</gray>");
                            mi.sendMessage(sender, "<#00ffe2>/AdvancedBan <reload/help></#00ffe2>");
                            mi.sendMessage(sender, "<dark_gray>»</dark_gray> <gray>Reloads the plugin or shows help page</gray>");
                            mi.sendMessage(sender, "");
                            return 1;
                        })
                        .then(Commands.literal("reload"))
                        .requires(sender -> sender.getSender().hasPermission("advancedban.reload"))
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            mi.loadFiles();
                            mi.sendMessage(sender, "<green><bold>ABX Velocity</bold></green> <dark_gray>»</dark_gray> <gray>Reloaded!</gray>");
                            return 1;
                        }).build(),
                commands.register(Commands.literal("systemprefs")
                        .executes(context -> {
                            MethodInterface mi = Universal.get().getMethods();
                            Calendar calendar = new GregorianCalendar();
                            CommandSender sender = context.getSource().getSender();
                            mi.sendMessage(sender, "<#00ffe2><bold>AdvancedBanX v3</bold> SystemPrefs</#00ffe2>");
                            mi.sendMessage(sender, "<#00ffe2>Server-Time</#00ffe2> <dark_gray>»</dark_gray> <gray>" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + "</gray>");
                            mi.sendMessage(sender, "<#00ffe2>Your UUID (Intern)</#00ffe2> <dark_gray>»</dark_gray> <gray>" + mi.getInternUUID(sender) + "</gray>");
                            return 0;
                        })
                        .then(Commands.argument("Target", ArgumentTypes.player())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getOnlinePlayers().stream()
                                            .map(Player::getName)
                                            .forEach(builder::suggest);
                                    return null;
                                })
                                .executes(context -> {
                                    //String target = input.getPrimaryData(); // Yes this is intentional
                                    CommandSender sender = context.getSource().getSender();
                                    String target = context.getArgument("Target");
                                    mi.sendMessage(sender, "<#00ffe2>" + target + "'s UUID (Intern)</#00ffe2> <dark_gray>»</dark_gray> <gray> <gray>" + mi.getInternUUID(target) + "</gray>");
                                    mi.sendMessage(sender, "<#00ffe2>" + target + "'s UUID (Fetched)</#00ffe2> <dark_gray>»</dark_gray> <gray> <gray>" + UUIDManager.get().getUUID(target) + "</gray>");

                                    return 1;
                                })
                                .build()


                        )


                ));
                commands.register(Commands.argument("Player", ArgumentTypes.player()))

                        .suggests((ctx, builder) -> {
                            Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .forEach(builder::suggest);
                            return null;
                        })
                        .executes(context -> {

                            String uuid = processName(context.getArgument("Player", PlayerSelectorArgumentResolver.class).resolve);
                            if (uuid == null)
                                return;
                            String name = context.getArgument("Player");

                            String ip = Universal.get().getIps().getOrDefault(name.toLowerCase(), "none cashed");
                            String loc = Universal.get().getMethods().getFromUrlJson("http://ip-api.com/json/" + ip, "country");
                            Punishment mute = PunishmentManager.get().getMute(uuid);
                            Punishment ban = PunishmentManager.get().getBan(uuid);

                            String cached = MessageManager.getMessage("Check.Cached", false);
                            String notCached = MessageManager.getMessage("Check.NotCached", false);

                            boolean nameCached = PunishmentManager.get().isCached(name.toLowerCase());
                            boolean ipCached = PunishmentManager.get().isCached(ip);
                            boolean uuidCached = PunishmentManager.get().isCached(uuid);

                            Object sender = context.getSender();
                            MessageManager.sendMessage(sender, "Check.Header", true, "NAME", name, "CACHED", nameCached ? cached : notCached);
                            MessageManager.sendMessage(sender, "Check.UUID", false, "UUID", uuid, "CACHED", uuidCached ? cached : notCached);
                            if (Universal.get().hasPerms(sender, "ab.check.ip")) {
                                MessageManager.sendMessage(sender, "Check.IP", false, "IP", ip, "CACHED", ipCached ? cached : notCached);
                            }
                            MessageManager.sendMessage(sender, "Check.Geo", false, "LOCATION", loc == null ? "failed!" : loc);
                            MessageManager.sendMessage(sender, "Check.Mute", false, "DURATION", mute == null ? "<green>none</green>" : mute.getType().isTemp() ? "<yellow>" + mute.getDuration(false) + "</yellow>" : "<#00ffe2>perma</#00ffe2>");
                            if (mute != null) {
                                MessageManager.sendMessage(sender, "Check.MuteReason", false, "REASON", mute.getReason());
                            }
                            MessageManager.sendMessage(sender, "Check.Ban", false, "DURATION", ban == null ? "<gree>none</green>" : ban.getType().isTemp() ? "<yellow>" + ban.getDuration(false) + "</yellow>" : "<#00ffe2>perma</#00ffe2>");
                            if (ban != null) {
                                MessageManager.sendMessage(sender, "Check.BanReason", false, "REASON", ban.getReason());
                            }
                            MessageManager.sendMessage(sender, "Check.Warn", false, "COUNT", PunishmentManager.get().getCurrentWarns(uuid) + "");

                            MessageManager.sendMessage(sender, "Check.Note", false, "COUNT", PunishmentManager.get().getCurrentNotes(uuid) + "");

                        });
    }

}