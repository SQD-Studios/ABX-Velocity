package net.hnt8.advancedban.utils;

import com.google.gson.Gson;
import net.hnt8.advancedban.utils.manager.UUIDManager;
import net.hnt8.advancedban.utils.util.Command;
import net.hnt8.advancedban.utils.util.InterimData;
import net.hnt8.advancedban.utils.util.Punishment;
import net.hnt8.advancedban.utils.manager.DatabaseManager;
import net.hnt8.advancedban.utils.manager.LogManager;
import net.hnt8.advancedban.utils.manager.PunishmentManager;
import net.hnt8.advancedban.utils.manager.UpdateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * This is the server independent entry point of the plugin.
 */
public class Universal {

    private static Universal instance = null;

    // Embed Modrinth settings at build time before shipping.
    private static final String MODRINTH_PROJECT = "3UQvsOJH";
    private static final String MODRINTH_TOKEN = "mrp_qVHCLBCGyFZ56TiOSnJTA4ZZa0EWDAqRdwC40crx9tC6aVnu1ATEqxcZ9Lg3";
    private static final String MODRINTH_API_BASE = "https://api.modrinth.com";

    public static void setRedis(boolean redis) {
        Universal.redis = redis;
    }

    private final Map<String, String> ips = new HashMap<>();
    private MethodInterface mi;
    private LogManager logManager;

    private static boolean redis = false;


    private final Gson gson = new Gson();

    /**
     * Get universal.
     *
     * @return the universal instance
     */
    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

    /**
     * Get AdvancedBanX's logger
     * 
     * @return the plugin logger
     */
    public Logger getLogger() {
        return mi.getLogger();
    }
    
    private String SerializeMiniMessage(String message) {
        Component messageComponent = MiniMessage.miniMessage().deserialize(message);
        return ANSIComponentSerializer.ansi().serialize(messageComponent);
    }
    
    /**
     * Initially sets up the plugin.
     *
     * @param mi the mi
     */
    public void setup(MethodInterface mi) {
        this.mi = mi;
        mi.loadFiles();
        logManager = new LogManager();
        UpdateManager.get().setup();
        UUIDManager.get().setup();

        try {
            DatabaseManager.get().setup(mi.getBoolean(mi.getConfig(), "UseMySQL", false));
        } catch (Exception ex) {
            getLogger().severe("Failed enabling database-manager...");
            debugException(ex);
        }

        mi.setupMetrics();
        PunishmentManager.get().setup();

        for (Command command : Command.values()) {
            for (String commandName : command.getNames()) {
                mi.setCommandExecutor(commandName, command.getPermission(), command.getTabCompleter());
            }
        }

        String upt = "You have the newest version";
        String modrinthProject = MODRINTH_PROJECT;
        String modrinthToken = MODRINTH_TOKEN;
        String response = fetchLatestModrinthVersion(modrinthProject, modrinthToken);
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else {
            int compare = compareVersions(mi.getVersion(), response);
            if (compare < 0) {
                upt = "There is a new version available! [" + response + "]";
            } else if (compare == 0) {
                upt = "You are up to date";
            } else {
                upt = "You are running a newer version than Modrinth! [" + response + "]";
            }
        }

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            String message = "\n\n<dark_gray>[]=====[<#00ffe2>Enabling ABX Velocity</#00ffe2>]=====[]</dark_gray>"
                           + "\n<dark_gray>|</dark_gray> <#00ffe2>Information:</#00ffe2>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>Name:</#00ffe2> <gray>ABX Velocity</gray>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>Developer:</#00ffe2> <gray>Chamogelastos</gray>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>Organization:</#00ffe2> <gray>SQD Studios</gray>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>Version:</#00ffe2> <gray>" + mi.getVersion() + "</gray>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>Storage:</#00ffe2> <gray>" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)") + "</gray>"
                           + "\n<dark_gray>|</dark_gray> <#00ffe2>Support:</#00ffe2>"
                           + "\n<dark_gray>|</dark_gray>   <#00ffe2>GitHub:</#00ffe2> <gray>https://github.com/SQD-Studios/ABX-Velocity/issues</gray>"
                           + "\n<dark_gray>|</dark_gray> <#00ffe2>Update:</#00ffe2>"
                           + "\n<dark_gray>|</dark_gray>   <gray>" + upt  + "</gray>"
                           + "\n<dark_gray>[]================================[]</dark_gray>\n ";
            
            mi.getLogger().info(SerializeMiniMessage(message));
        } else {
            mi.getLogger().info(SerializeMiniMessage("<#00ffe2>Enabling AdvancedBanX on Version</#00ffe2> <gray>" + mi.getVersion() + "</gray>"));
            mi.getLogger().info(SerializeMiniMessage("<#00ffe2>Coded by <gray>Leoko</gray> <dark_gray>|</dark_gray> Maintained & Updated by <gray>2vY</gray></#00ffe2>"));
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        DatabaseManager.get().shutdown();

        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            String message = "\n\n<dark_gray>[]=====[<#00ffe2>Disabling ABX Velocity</#00ffe2>]=====[]</dark_gray>"
                    + "\n<dark_gray>|</dark_gray> <#00ffe2>Information:</#00ffe2>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>Name:</#00ffe2> <gray>ABX Velocity</gray>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>Developer:</#00ffe2> <gray>Chamogelastos</gray>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>Organization:</#00ffe2> <gray>SQD Studios</gray>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>Version:</#00ffe2> <gray>" + mi.getVersion() + "</gray>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>Storage:</#00ffe2> <gray>" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)") + "</gray>"
                    + "\n<dark_gray>|</dark_gray> <#00ffe2>Support:</#00ffe2>"
                    + "\n<dark_gray>|</dark_gray>   <#00ffe2>GitHub:</#00ffe2> <gray>https://github.com/SQD-Studios/ABX-Velocity/issues</gray>"
                    + "\n<dark_gray>[]================================[]</dark_gray>\n ";
            
            mi.getLogger().info(SerializeMiniMessage(message));
        } else {
            mi.getLogger().info(SerializeMiniMessage("<#00ffe2>Disabling ABX Velocity on Version</#00ffe2> <gray>" + mi.getVersion() + "</gray>"));
            mi.getLogger().info(SerializeMiniMessage("<#00ffe2>Coded by <gray>Chamogelastos</gray> <dark_gray>|</dark_gray> Organization:<gray>SQD Studios</gray></#00ffe2>"));
        }
    }

    /**
     * Gets methods.
     *
     * @return the methods
     */
    public MethodInterface getMethods() {
        return mi;
    }

    /**
     * Is bungee boolean.
     *
     * @return the boolean
     */
    public boolean isBungee() {
        return mi.isBungee();
    }

    public Map<String, String> getIps() {
        return ips;
    }

    public static boolean isRedis() {
        return redis;
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Gets from url.
     *
     * @param surl the surl
     * @return the from url
     */
    public String getFromURL(String surl) {
        return getFromURL(surl, new HashMap<>());
    }

    public String getFromURL(String surl, Map<String, String> headers) {
        HttpResult result = getFromURLWithStatus(surl, headers);
        return result == null ? null : result.body;
    }

    private HttpResult getFromURLWithStatus(String surl, Map<String, String> headers) {
        HttpURLConnection connection = null;
        int status = -1;
        String response = null;
        try {
            URL url = new URL(surl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "ABX-Velocity");
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    if (header.getKey() != null && header.getValue() != null) {
                        connection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }
            }

            status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            if (stream == null) {
                return new HttpResult(status, null);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                response = builder.toString();
            }
        } catch (IOException exc) {
            getLogger().warning("!! Failed to connect to URL: " + surl);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new HttpResult(status, response);
    }

    private String fetchLatestModrinthVersion(String projectInput, String token) {
        String project = normalizeModrinthProject(projectInput);
        if (project == null || project.isEmpty()) {
            logUpdateWarning("Modrinth project is empty. Update check skipped.");
            return null;
        }

        String url = MODRINTH_API_BASE + "/v2/project/" + project + "/version";
        Map<String, String> headers = new HashMap<>();
        if (token != null && !token.trim().isEmpty()) {
            String trimmedToken = token.trim();
            if (trimmedToken.toLowerCase().startsWith("bearer ")) {
                headers.put("Authorization", trimmedToken);
            } else {
                headers.put("Authorization", trimmedToken);
            }
        }

        logUpdateDebug("Requesting latest Modrinth version from " + url);
        HttpResult result = getFromURLWithStatus(url, headers);
        if (result == null || result.body == null || result.body.isEmpty()) {
            logUpdateWarning("No response body from Modrinth (status=" + (result == null ? "unknown" : result.status) + ").");
            return null;
        }
        if (result.status < 200 || result.status >= 300) {
            logUpdateWarning("Modrinth API returned status " + result.status + " with body: " + safeSnippet(result.body));
            return null;
        }

        try {
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(result.body);
            if (!root.isJsonArray()) {
                logUpdateWarning("Modrinth API did not return a JSON array. Body: " + safeSnippet(result.body));
                return null;
            }

            com.google.gson.JsonArray versions = root.getAsJsonArray();
            if (versions.size() == 0) {
                logUpdateWarning("Modrinth project has no versions.");
                return null;
            }
            Instant latestDate = null;
            String latestVersion = null;

            for (com.google.gson.JsonElement element : versions) {
                if (!element.isJsonObject()) {
                    continue;
                }
                com.google.gson.JsonObject obj = element.getAsJsonObject();
                if (!obj.has("version_number") || !obj.has("date_published")) {
                    continue;
                }
                String versionNumber = obj.get("version_number").getAsString();
                String datePublished = obj.get("date_published").getAsString();
                Instant publishedAt;
                try {
                    publishedAt = Instant.parse(datePublished);
                } catch (Exception ignored) {
                    continue;
                }

                if (latestDate == null || publishedAt.isAfter(latestDate)) {
                    latestDate = publishedAt;
                    latestVersion = versionNumber;
                }
            }

            if (latestVersion == null) {
                logUpdateWarning("Could not determine latest Modrinth version from response.");
            }
            return latestVersion;
        } catch (Exception ex) {
            debugException(ex);
            return null;
        }
    }

    private String normalizeModrinthProject(String projectInput) {
        if (projectInput == null) {
            return "";
        }
        String trimmed = projectInput.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("modrinth.com")) {
            int lastSlash = trimmed.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash + 1 < trimmed.length()) {
                String tail = trimmed.substring(lastSlash + 1);
                int query = tail.indexOf('?');
                if (query != -1) {
                    tail = tail.substring(0, query);
                }
                if (tail.endsWith("/")) {
                    tail = tail.substring(0, tail.length() - 1);
                }
                return tail.trim();
            }
        }
        return trimmed;
    }

    private int compareVersions(String localVersion, String remoteVersion) {
        String local = normalizeVersion(localVersion);
        String remote = normalizeVersion(remoteVersion);

        if (local.isEmpty() || remote.isEmpty()) {
            return 0;
        }
        if (local.equalsIgnoreCase(remote)) {
            return 0;
        }

        VersionParts localParts = VersionParts.parse(local);
        VersionParts remoteParts = VersionParts.parse(remote);
        if (localParts != null && remoteParts != null) {
            return localParts.compareTo(remoteParts);
        }

        if (local.startsWith(remote)) {
            return 0;
        }
        if (remote.startsWith(local)) {
            return -1;
        }
        return -1;
    }

    private String normalizeVersion(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int space = trimmed.indexOf(' ');
        if (space != -1) {
            trimmed = trimmed.substring(0, space);
        }
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            trimmed = trimmed.substring(1);
        }
        int plusIndex = trimmed.indexOf('+');
        if (plusIndex != -1) {
            trimmed = trimmed.substring(0, plusIndex);
        }
        return trimmed;
    }

    private static final class VersionParts implements Comparable<VersionParts> {
        private final int[] numbers;
        private final String suffix;

        private VersionParts(int[] numbers, String suffix) {
            this.numbers = numbers;
            this.suffix = suffix == null ? "" : suffix;
        }

        static VersionParts parse(String version) {
            if (version == null || version.isEmpty()) {
                return null;
            }
            String[] mainAndSuffix = version.split("-", 2);
            String main = mainAndSuffix[0];
            String suffix = mainAndSuffix.length > 1 ? mainAndSuffix[1] : "";
            String[] segments = main.split("\\.");
            int[] numbers = new int[segments.length];
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if (!segment.matches("\\d+")) {
                    return null;
                }
                numbers[i] = Integer.parseInt(segment);
            }
            return new VersionParts(numbers, suffix);
        }

        @Override
        public int compareTo(VersionParts other) {
            int max = Math.max(this.numbers.length, other.numbers.length);
            for (int i = 0; i < max; i++) {
                int left = i < this.numbers.length ? this.numbers[i] : 0;
                int right = i < other.numbers.length ? other.numbers[i] : 0;
                if (left != right) {
                    return Integer.compare(left, right);
                }
            }

            boolean hasSuffix = this.suffix != null && !this.suffix.isEmpty();
            boolean otherHasSuffix = other.suffix != null && !other.suffix.isEmpty();
            if (hasSuffix && !otherHasSuffix) {
                return -1;
            }
            if (!hasSuffix && otherHasSuffix) {
                return 1;
            }
            return this.suffix.compareToIgnoreCase(other.suffix);
        }
    }

    private void logUpdateWarning(String message) {
        getLogger().warning("Update check: " + message);
    }

    private void logUpdateDebug(String message) {
        if (mi != null && mi.getBoolean(mi.getConfig(), "Debug", false)) {
            getLogger().info("Update check: " + message);
        }
    }

    private String safeSnippet(String value) {
        if (value == null) {
            return "null";
        }
        int limit = 300;
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }

    private static final class HttpResult {
        private final int status;
        private final String body;

        private HttpResult(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    /**
     * Is mute command boolean.
     *
     * @param cmd the cmd
     * @return the boolean
     */
    public boolean isMuteCommand(String cmd) {
        return isMuteCommand(cmd, getMethods().getStringList(getMethods().getConfig(), "MuteCommands"));
    }

    /**
     * Visible for testing. Do not use this. Please use {@link #isMuteCommand(String)}.
     * 
     * @param cmd          the command
     * @param muteCommands the mute commands from the config
     * @return true if the command matched any of the mute commands.
     */
    boolean isMuteCommand(String cmd, List<String> muteCommands) {
        String[] words = cmd.split(" ");
        // Handle commands with colons
        if (words[0].indexOf(':') != -1) {
            words[0] = words[0].split(":", 2)[1];
        }
        for (String muteCommand : muteCommands) {
            if (muteCommandMatches(words, muteCommand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visible for testing. Do not use this.
     * 
     * @param commandWords the command run by a player, separated into its words
     * @param muteCommand a mute command from the config
     * @return true if they match, false otherwise
     */
    boolean muteCommandMatches(String[] commandWords, String muteCommand) {
        // Basic equality check
        if (commandWords[0].equalsIgnoreCase(muteCommand)) {
            return true;
        }
        // Advanced equality check
        // Essentially a case-insensitive "startsWith" for arrays
        if (muteCommand.indexOf(' ') != -1) {
            String[] muteCommandWords = muteCommand.split(" ");
            if (muteCommandWords.length > commandWords.length) {
                return false;
            }
            for (int n = 0; n < muteCommandWords.length; n++) {
                if (!muteCommandWords[n].equalsIgnoreCase(commandWords[n])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Is exempt player boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean isExemptPlayer(String name) {
        List<String> exempt = getMethods().getStringList(getMethods().getConfig(), "ExemptPlayers");
        if (exempt != null) {
            for (String str : exempt) {
                if (name.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Broadcast leoko boolean.
     *
     * @return the boolean
     */
    public boolean broadcastLeoko() {
        File readme = new File(getMethods().getDataFolder(), "readme.txt");
        if (!readme.exists()) {
            return true;
        }
        try {
            if (Files.readAllLines(Paths.get(readme.getPath()), Charset.defaultCharset()).get(0).equalsIgnoreCase("I don't want that there will be any message when the dev of this plugin joins the server! I want this even though the plugin is 100% free and the join-message is the only reward for the Dev :(")) {
                return false;
            }
        } catch (IOException ignore) {
        }
        return true;
    }

    /**
     * Call connection string.
     *
     * @param name the name
     * @param ip   the ip
     * @return the string
     */
    public String callConnection(String name, String ip) {
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
        if (uuid == null) return "[AdvancedBan] Failed to fetch your UUID";

        if (ip != null) {
            getIps().remove(name);
            getIps().put(name, ip);
        }

        InterimData interimData = PunishmentManager.get().load(name, uuid, ip);

        if (interimData == null) {
            if (getMethods().getBoolean(mi.getConfig(), "LockdownOnError", true)) {
                return "[AdvancedBan] Failed to load player data!";
            } else {
                return null;
            }
        }

        Punishment pt = interimData.getBan();

        if (pt == null) {
            interimData.accept();
            return null;
        }

        return pt.getLayoutBSN();
    }

    /**
     * Has perms boolean.
     *
     * @param player the player
     * @param perms  the perms
     * @return the boolean
     */
    public boolean hasPerms(Object player, String perms) {
        if (mi.hasPerms(player, perms)) {
            return true;
        }

        if (mi.getBoolean(mi.getConfig(), "EnableAllPermissionNodes", false)) {
            while (perms.contains(".")) {
                perms = perms.substring(0, perms.lastIndexOf('.'));
                if (mi.hasPerms(player, perms + ".all")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void debugException(Exception exc) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        getLogger().fine(sw.toString());
    }

    /**
     * Debug.
     *
     * @param ex the ex
     */
    public void debugSqlException(SQLException ex) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            getLogger().fine(SerializeMiniMessage("<gray>An error has occurred with the database, the error code is: '" + ex.getErrorCode() + "'</gray>"));
            getLogger().fine(SerializeMiniMessage("<gray>The state of the sql is: " + ex.getSQLState() + "</gray>"));
            getLogger().fine(SerializeMiniMessage("<gray>Error message: " + ex.getMessage() + "</gray>"));
        }
        debugException(ex);
    }

    private void debugToFile(Object msg) {
        File debugFile = new File(mi.getDataFolder(), "logs/latest.log");
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile();
            } catch (IOException ex) {
                Universal.get().getMethods().getLogger().warning("An error has occurred creating the 'latest.log' file again, check your server.");
                Universal.get().getMethods().getLogger().warning("Error message" + ex.getMessage());
            }
        } else {
            logManager.checkLastLog(false);
        }
        try {
            FileUtils.writeStringToFile(debugFile, "[" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + "] " + mi.clearFormatting(msg.toString()) + "\n", "UTF8", true);
        } catch (IOException ex) {
            Universal.get().getMethods().getLogger().warning("An error has occurred writing to 'latest.log' file.");
            Universal.get().getMethods().getLogger().warning(ex.getMessage());
        }
    }
}
