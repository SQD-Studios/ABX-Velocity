package net.chamosmp.aBXVelocity.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.chamosmp.aBXVelocity.VelocityMain;
import net.hnt8.advancedban.utils.utils.Punishment;
import net.hnt8.advancedban.utils.utils.PunishmentType;
import net.hnt8.advancedban.utils.Universal;
import net.hnt8.advancedban.utils.manager.TimeManager;
import net.hnt8.advancedban.utils.manager.UUIDManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class VelocityPluginMessageListener {

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        ChannelIdentifier mainChannel = VelocityMain.get().getMainChannel();
        ChannelIdentifier connectionChannel = VelocityMain.get().getConnectionChannel();

        if (!event.getIdentifier().equals(mainChannel) && !event.getIdentifier().equals(connectionChannel)) {
            return;
        }

        if (event.getSource() instanceof Player) {
            return;
        }

        ByteArrayDataInput in = event.dataAsDataStream();

        if (event.getIdentifier().equals(connectionChannel)) {
            String message = in.readUTF();
            String[] parts = message.split(",", 2);
            if (parts.length == 2) {
                Universal.get().getIps().put(parts[0].toLowerCase(), parts[1]);
            }
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            return;
        }

        String channel = in.readUTF();
        if ("Punish".equals(channel)) {
            String message = in.readUTF();
            try {
                JsonObject punishment = Universal.get().getGson().fromJson(message, JsonObject.class);
                new Punishment(
                        punishment.get("name").getAsString(),
                        UUIDManager.get().getUUID(punishment.get("uuid").getAsString()),
                        punishment.get("reason").getAsString(),
                        punishment.get("operator") != null ? punishment.get("operator").getAsString() : "CONSOLE",
                        PunishmentType.valueOf(punishment.get("punishmenttype").getAsString().toUpperCase()),
                        punishment.get("start") != null ? punishment.get("start").getAsLong() : TimeManager.getTime(),
                        TimeManager.getTime() + punishment.get("end").getAsLong(),
                        punishment.get("calculation") != null ? punishment.get("calculation").getAsString() : null,
                        -1
                ).create(punishment.get("silent") != null && punishment.get("silent").getAsBoolean());
                Universal.get().getLogger().info("A punishment was created using PluginMessaging listener.");
                Universal.get().getLogger().fine(punishment.toString());
            } catch (JsonSyntaxException | NullPointerException ex) {
                Universal.get().getLogger().severe("An exception occurred while reading a punishment from plugin messaging channel.");
                Universal.get().getLogger().fine("Message: " + message);
                Universal.get().getLogger().fine("StackTrace:");
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                ex.printStackTrace(printWriter);
                Universal.get().getLogger().fine(stringWriter.toString());
            }
        } else {
            Universal.get().getLogger().fine("Unknown channel for tag \"AdvancedBan\"");
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    public static void sendToServers(String channel, String payload) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(payload);
        for (RegisteredServer server : VelocityMain.get().getProxy().getAllServers()) {
            server.sendPluginMessage(VelocityMain.get().getMainChannel(), out.toByteArray());
        }
    }

    public static void sendToServers(String channel, Iterable<String> messages) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        for (String msg : messages) {
            out.writeUTF(msg);
        }
        for (RegisteredServer server : VelocityMain.get().getProxy().getAllServers()) {
            server.sendPluginMessage(VelocityMain.get().getMainChannel(), out.toByteArray());
        }
    }
}
