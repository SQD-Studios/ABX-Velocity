package net.hnt8.advancedban.utils.util.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPI extends PlaceholderExpansion {

    @Override
    @NotNull
    public String getAuthor() {
        return "SQD Studios"; //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "ABX Velocity"; //
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0"; //
    }

    // These methods aren't overriden by default.
    // You have to override one of them.

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        //
        return params;

    }
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("placeholder1")) {
            return "text1";
        }

        if (params.equalsIgnoreCase("placeholder2")) {
            return "text2";
        }
        return params;
    }}