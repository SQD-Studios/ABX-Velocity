package net.hnt8.advancedban.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hnt8.advancedban.MethodInterface;
import net.hnt8.advancedban.Universal;

public class DynamicDataSource {
    private final HikariConfig config = new HikariConfig();

    public DynamicDataSource(boolean preferMySQL) throws ClassNotFoundException {
        MethodInterface mi = Universal.get().getMethods();

        if (preferMySQL) {
            String ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
            String dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
            String usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
            String password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
            String properties = mi.getString(mi.getMySQLFile(), "MySQL.Properties",
                    "useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8");
            int port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);

            Class.forName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?" + properties);
            config.setUsername(usrName);
            config.setPassword(password);
        } else {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
            config.setJdbcUrl("jdbc:hsqldb:file:" + mi.getDataFolder().getPath() + "/data/storage;hsqldb.lock_file=false");
            config.setUsername("SA");
            config.setPassword("");
        }

        // HikariCP pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setPoolName("AdvancedBanPool");
    }

    public HikariDataSource generateDataSource() {
        try {
            HikariDataSource ds = new HikariDataSource(config);
            ds.getConnection().close(); // Test connection immediately
            return ds;
        } catch (Exception ex) {
            Universal.get().getLogger().severe("Failed to initialize database connection: " + ex.getMessage());
            Universal.get().debugException(ex);
            return null;
        }
    }
}
