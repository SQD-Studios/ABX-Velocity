package net.hnt8.advancedban.utils.manager;

import com.zaxxer.hikari.HikariDataSource;
import net.hnt8.advancedban.utils.util.DynamicDataSource;
import net.hnt8.advancedban.utils.util.SQLQuery;
import net.hnt8.advancedban.utils.Universal;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Robust database manager for AdvancedBanX.
 * Supports MySQL or embedded HSQLDB.
 */
public class DatabaseManager {

    private HikariDataSource dataSource;
    private boolean useMySQL;
    private RowSetFactory factory;

    private static DatabaseManager instance;

    public static synchronized DatabaseManager get() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initializes the database and ensures tables exist.
     *
     * @param useMySQLServer true to use MySQL, false for HSQLDB
     */
    public void setup(boolean useMySQLServer) {
        this.useMySQL = useMySQLServer;

        try {
            dataSource = new DynamicDataSource(useMySQL).generateDataSource();
            if (dataSource == null) {
                throw new IllegalStateException("Failed to generate DataSource.");
            }
            Universal.get().getLogger().info("Database initialized using " + (useMySQL ? "MySQL" : "HSQLDB"));
        } catch (Exception ex) {
            Universal.get().getLogger().severe("Database setup failed: " + ex.getMessage());
            Universal.get().debugException(ex);
            return; // Stop setup on failure
        }

        // Create required tables
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
    }

    /**
     * Shuts down the database safely.
     */
    public void shutdown() {
        if (dataSource == null) return;

        if (!useMySQL) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SHUTDOWN")) {
                stmt.execute();
                Universal.get().getLogger().info("HSQLDB shutdown successfully.");
            } catch (SQLException ex) {
                Universal.get().getLogger().warning("Failed to shutdown HSQLDB: " + ex.getMessage());
                Universal.get().debugException(ex);
            }
        }

        try {
            dataSource.close();
            Universal.get().getLogger().info("Database connection pool closed.");
        } catch (Exception ex) {
            Universal.get().getLogger().warning("Failed to close DataSource: " + ex.getMessage());
            Universal.get().debugException(ex);
        }
    }

    private CachedRowSet createCachedRowSet() throws SQLException {
        if (factory == null) factory = RowSetProvider.newFactory();
        return factory.createCachedRowSet();
    }

    public void executeStatement(SQLQuery sql, Object... parameters) {
        executeStatement(sql.toString(), false, parameters);
    }

    public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
        return executeStatement(sql.toString(), true, parameters);
    }

    private synchronized ResultSet executeStatement(String sql, boolean expectResult, Object... parameters) {
        if (dataSource == null) {
            Universal.get().getLogger().severe("Cannot execute query: DataSource is not initialized!");
            return null;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }

            if (expectResult) {
                CachedRowSet rowSet = createCachedRowSet();
                rowSet.populate(stmt.executeQuery());
                return rowSet;
            } else {
                stmt.execute();
            }
        } catch (SQLException ex) {
            Universal.get().getLogger().severe("SQL execution error: " + ex.getMessage());
            Universal.get().getLogger().fine("Query: " + sql);
            Universal.get().debugSqlException(ex);
        }

        return null;
    }

    public boolean isConnectionValid() {
        return dataSource != null && !dataSource.isClosed();
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }
}