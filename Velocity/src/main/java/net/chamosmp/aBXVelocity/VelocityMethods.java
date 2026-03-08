package net.chamosmp.aBXVelocity;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.hnt8.advancedban.utils.MethodInterface;
import net.hnt8.advancedban.utils.util.Permissionable;
import net.hnt8.advancedban.utils.util.Punishment;
import net.hnt8.advancedban.utils.Universal;
import net.hnt8.advancedban.utils.manager.DatabaseManager;
import net.hnt8.advancedban.utils.manager.PunishmentManager;
import net.hnt8.advancedban.utils.manager.UUIDManager;
import net.hnt8.advancedban.utils.util.tabcompletion.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

public class VelocityMethods implements MethodInterface {

    private final VelocityMain plugin;
    private final ProxyServer proxy;

    private final File configFile;
    private final File messageFile;
    private final File layoutFile;
    private final File mysqlFile;

    private net.chamosmp.aBXVelocity.velocity.YamlConfig config;
    private net.chamosmp.aBXVelocity.velocity.YamlConfig messages;
    private net.chamosmp.aBXVelocity.velocity.YamlConfig layouts;
    private net.chamosmp.aBXVelocity.velocity.YamlConfig mysql;

    private final Function<String, Permissionable> permissionableGenerator;

    public VelocityMethods(VelocityMain plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getProxy();

        File dataFolder = getDataFolder();
        this.configFile = new File(dataFolder, "config.yml");
        this.messageFile = new File(dataFolder, "Messages.yml");
        this.layoutFile = new File(dataFolder, "Layouts.yml");
        this.mysqlFile = new File(dataFolder, "MySQL.yml");

        if (proxy.getPluginManager().getPlugin("luckperms").isPresent()) {
            permissionableGenerator = net.chamosmp.aBXVelocity.utils.LuckPermsOfflineUser::new;
            getLogger().info("[AdvancedBanX] Offline permission support through LuckPerms active");
        } else {
            permissionableGenerator = null;
            getLogger().info("[AdvancedBanX] No offline permission support through LuckPerms");
        }
    }

    @Override
    public void loadFiles() {
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            copyResourceIfMissing("config.yml", configFile);
            copyResourceIfMissing("Messages.yml", messageFile);
            copyResourceIfMissing("Layouts.yml", layoutFile);

            config = net.chamosmp.aBXVelocity.velocity.YamlConfig.load(configFile);
            messages = net.chamosmp.aBXVelocity.velocity.YamlConfig.load(messageFile);
            layouts = net.chamosmp.aBXVelocity.velocity.YamlConfig.load(layoutFile);

            if (mysqlFile.exists()) {
                mysql = net.chamosmp.aBXVelocity.velocity.YamlConfig.load(mysqlFile);
            } else {
                mysql = config;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyResourceIfMissing(String name, File target) throws IOException {
        if (target.exists()) {
            return;
        }
        try (InputStream in = VelocityMain.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                return;
            }
            Files.copy(in, target.toPath());
        }
    }

    @Override
    public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonObject json = (JsonObject) jp.parse(new InputStreamReader(request.getInputStream()));

            String[] keys = key.split("\\|");
            for (int i = 0; i < keys.length - 1; i++) {
                json = json.getAsJsonObject(keys[i]);
            }

            return json.get(keys[keys.length - 1]).toString().replaceAll("\"", "");
        } catch (Exception exc) {
            return null;
        }
    }

    @Override
    public String getVersion() {
        return proxy.getPluginManager()
                .fromInstance(plugin)
                .flatMap(p -> p.getDescription().getVersion())
                .orElse("unknown");
    }

    @Override
    public String[] getKeys(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getKeys(path).toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public Object getConfig() {
        return config;
    }

    @Override
    public Object getMessages() {
        return messages;
    }

    @Override
    public Object getLayouts() {
        return layouts;
    }

    @Override
    public void setupMetrics() {
        Metrics metrics = plugin.getMetricsFactory().make(plugin, 29953);
        metrics.addCustomChart(new SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public String clearFormatting(String text) {
        String noSection = text.replaceAll("(?i)§[0-9A-FK-OR]", "");
        return noSection.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataDirectory().toFile();
    }

    @Override
    public void setCommandExecutor(String cmd, String permission, TabCompleter tabCompleter) {
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder(cmd).plugin(plugin).build(),
                new net.chamosmp.aBXVelocity.velocity.VelocitySimpleCommand(cmd, permission, tabCompleter)
        );
    }

    @Override
    public void sendMessage(Object player, String msg) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextReplacementConfig replacementConfig = TextReplacementConfig.builder().matchLiteral("&").replacement("§").build();
        Component msgComponent = miniMessage.deserialize(msg).replaceText(replacementConfig);

        if (player instanceof CommandSource source) {
            source.sendMessage(msgComponent);
        } else if (player instanceof Player p) {
            p.sendMessage(msgComponent);
        }
    }

    @Override
    public String getName(Object player) {
        if (player instanceof Player p) {
            return p.getUsername();
        }
        if (player instanceof CommandSource) {
            return "CONSOLE";
        }
        return null;
    }

    @Override
    public String getName(String uuid) {
        return proxy.getPlayer(UUID.fromString(uuid)).map(Player::getUsername).orElse(null);
    }

    @Override
    public String getIP(Object player) {
        if (player instanceof Player p && p.getRemoteAddress() != null) {
            return p.getRemoteAddress().getAddress().getHostAddress();
        }
        return null;
    }

    @Override
    public String getInternUUID(Object player) {
        if (player instanceof Player p) {
            return p.getUniqueId().toString().replaceAll("-", "");
        }
        return "none";
    }

    @Override
    public String getInternUUID(String player) {
        return proxy.getPlayer(player)
                .map(p -> p.getUniqueId().toString().replaceAll("-", ""))
                .orElse(null);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        if (player instanceof CommandSource source) {
            return source.hasPermission(perms);
        }
        return false;
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String name) {
        if (permissionableGenerator != null) {
            return permissionableGenerator.apply(name);
        }
        return permission -> false;
    }

    @Override
    public boolean isOnline(String name) {
        return proxy.getPlayer(name).isPresent();
    }

    @Override
    public Object getPlayer(String name) {
        return proxy.getPlayer(name).orElse(null);
    }

    @Override
    public void kickPlayer(String player, String reason) {
        proxy.getPlayer(player).ifPresent(p -> {
            Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(reason.replace('§', '&'));
            if (VelocityMain.get().getCloudSupport() != null) {
                VelocityMain.get().getCloudSupport().kick(p.getUniqueId(), PlainTextComponentSerializer.plainText().serialize(comp));
            } else {
                p.disconnect(comp);
            }
        });
    }

    @Override
    public Object[] getOnlinePlayers() {
        return proxy.getAllPlayers().toArray();
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        proxy.getScheduler().buildTask(plugin, rn)
                .delay(l1 * 50, TimeUnit.MILLISECONDS)
                .repeat(l2 * 50, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        proxy.getScheduler().buildTask(plugin, rn)
                .delay(l1 * 50, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    public void runAsync(Runnable rn) {
        proxy.getScheduler().buildTask(plugin, rn).schedule();
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
    }

    @Override
    public void executeCommand(String cmd) {
        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), cmd);
    }

    @Override
    public boolean callChat(Object player) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)));
        if (pnt != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callCMD(Object player, String cmd) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(cmd.substring(1))
                && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public Object getMySQLFile() {
        return mysql;
    }

    @Override
    public String parseJSON(InputStreamReader json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public String parseJSON(String json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getBoolean(path);
        }
        return null;
    }

    @Override
    public String getString(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getString(path);
        }
        return null;
    }

    @Override
    public Long getLong(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getLong(path);
        }
        return null;
    }

    @Override
    public Integer getInteger(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getInteger(path);
        }
        return null;
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.getStringList(path);
        }
        return List.of();
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        Boolean value = getBoolean(file, path);
        return value != null ? value : def;
    }

    @Override
    public String getString(Object file, String path, String def) {
        String value = getString(file, path);
        return value != null ? value : def;
    }

    @Override
    public long getLong(Object file, String path, long def) {
        Long value = getLong(file, path);
        return value != null ? value : def;
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        Integer value = getInteger(file, path);
        return value != null ? value : def;
    }

    @Override
    public boolean contains(Object file, String path) {
        if (file instanceof net.chamosmp.aBXVelocity.velocity.YamlConfig cfg) {
            return cfg.get(path) != null;
        }
        return false;
    }

    @Override
    public String getFileName(Object file) {
        return "[Only available on Bukkit-Version!]";
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        net.chamosmp.aBXVelocity.velocity.VelocityPluginMessageListener.sendToServers(
                "Punish",
                List.of(punishment.toString())
        );
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        net.chamosmp.aBXVelocity.velocity.VelocityPluginMessageListener.sendToServers(
                "Unpunish",
                List.of(punishment.toString())
        );
    }

    @Override
    public boolean isOnlineMode() {
        return proxy.getConfiguration().isOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        for (Player player : proxy.getAllPlayers()) {
            if (Universal.get().hasPerms(player, perm)) {
                for (String str : notification) {
                    sendMessage(player, str);
                }
            }
        }
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }
}
