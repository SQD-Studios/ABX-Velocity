package net.hnt8.advancedban.bukkit;

public class checkfolia {

    private static boolean isFolia() {
    try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        return true;
    } catch (ClassNotFoundException e) {
        return false;
    }
}

}
