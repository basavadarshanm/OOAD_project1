package com.onlinebanking.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Creates pooled JDBC DataSources based on application.properties.
 */
public final class DataSourceFactory {
    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

    private DataSourceFactory() {
    }

    public static HikariDataSource createFromProperties() {
        Properties props = new Properties();
        try (InputStream in = DataSourceFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load database configuration", e);
        }

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String url = firstNonBlank(dotenv.get("DB_URL"), props.getProperty("db.url"));
        String user = firstNonBlank(dotenv.get("DB_USER"), props.getProperty("db.user"));
        String password = firstNonBlank(dotenv.get("DB_PASSWORD"), props.getProperty("db.password"));

        if (url == null) {
            throw new IllegalStateException("DB_URL must be set (via .env or application.properties)");
        }

        HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                if (user != null) {
                        config.setUsername(user);
                }
                if (password != null) {
                        config.setPassword(password);
                }
        config.setMaximumPoolSize(parseInt(dotenv.get("POOL_MAXIMUM_POOL_SIZE"), props.getProperty("pool.maximumPoolSize"), 10));
        config.setConnectionTimeout(parseLong(dotenv.get("POOL_CONNECTION_TIMEOUT_MS"), props.getProperty("pool.connectionTimeoutMs"), 30000));
        config.setIdleTimeout(parseLong(dotenv.get("POOL_IDLE_TIMEOUT_MS"), props.getProperty("pool.idleTimeoutMs"), 600000));
        config.setMaxLifetime(parseLong(dotenv.get("POOL_MAX_LIFETIME_MS"), props.getProperty("pool.maxLifetimeMs"), 1800000));
        config.setPoolName("OnlineBankingPool");

        log.info("Initialized datasource for {}", config.getJdbcUrl());
                HikariDataSource ds = new HikariDataSource(config);
                initializeSchema(ds);
                return ds;
    }

    private static void initializeSchema(HikariDataSource ds) {
        String ddl = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(64) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(16) NOT NULL
                );
                CREATE TABLE IF NOT EXISTS accounts (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL UNIQUE,
                    account_number VARCHAR(32) UNIQUE NOT NULL,
                    phone_number VARCHAR(16) UNIQUE,
                    balance DECIMAL(15,2) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                CREATE TABLE IF NOT EXISTS transactions (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    from_account_id BIGINT NOT NULL,
                    to_account_id BIGINT,
                    transaction_type VARCHAR(20) NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    description VARCHAR(255),
                    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
                    FOREIGN KEY (to_account_id) REFERENCES accounts(id)
                );
                CREATE TABLE IF NOT EXISTS beneficiaries (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    name VARCHAR(128) NOT NULL,
                    account_number VARCHAR(32) NOT NULL,
                    bank VARCHAR(128) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                CREATE TABLE IF NOT EXISTS bill_payments (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    account_id BIGINT NOT NULL,
                    biller_name VARCHAR(100) NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    bill_reference_number VARCHAR(100),
                    due_date DATE,
                    paid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL DEFAULT 'PAID',
                    transaction_id BIGINT,
                    FOREIGN KEY (account_id) REFERENCES accounts(id),
                    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
                );
                """;
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            migrateLegacySchema(conn);
            log.info("Ensured schema is present and compatible");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize schema", e);
        }
    }

    private static void migrateLegacySchema(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // Accounts migration: add phone_number if missing.
            stmt.execute("ALTER TABLE accounts ADD COLUMN IF NOT EXISTS phone_number VARCHAR(16)");

            // Transactions migration: support old columns (account_id, type, occurred_at).
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS from_account_id BIGINT");
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS to_account_id BIGINT");
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS transaction_type VARCHAR(20)");
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS status VARCHAR(20)");
            stmt.execute("ALTER TABLE transactions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");

            if (hasColumn(conn, "TRANSACTIONS", "ACCOUNT_ID")) {
                stmt.execute("UPDATE transactions SET from_account_id = account_id WHERE from_account_id IS NULL");
            }
            if (hasColumn(conn, "TRANSACTIONS", "TYPE")) {
                stmt.execute("UPDATE transactions SET transaction_type = type WHERE transaction_type IS NULL");
            }
            if (hasColumn(conn, "TRANSACTIONS", "OCCURRED_AT")) {
                stmt.execute("UPDATE transactions SET created_at = occurred_at WHERE created_at IS NULL");
            }
            stmt.execute("UPDATE transactions SET status = 'COMPLETED' WHERE status IS NULL");
            stmt.execute("UPDATE transactions SET transaction_type = 'TRANSFER' WHERE transaction_type IS NULL");
            stmt.execute("UPDATE transactions SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");
        }

        ensureUniqueIndex(conn, "ACCOUNTS", "UK_ACCOUNTS_PHONE", "phone_number");
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void ensureUniqueIndex(Connection conn, String tableName, String indexName, String columnName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getIndexInfo(null, null, tableName, true, false)) {
            while (rs.next()) {
                String existing = rs.getString("INDEX_NAME");
                if (indexName.equalsIgnoreCase(existing)) {
                    return;
                }
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnName + ")");
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static int parseInt(String primary, String fallback, int defaultVal) {
        String value = firstNonBlank(primary, fallback);
        return value == null ? defaultVal : Integer.parseInt(value);
    }

    private static long parseLong(String primary, String fallback, long defaultVal) {
        String value = firstNonBlank(primary, fallback);
        return value == null ? defaultVal : Long.parseLong(value);
    }
}
