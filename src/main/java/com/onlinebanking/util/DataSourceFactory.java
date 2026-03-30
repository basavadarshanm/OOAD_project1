package com.onlinebanking.util;

import java.io.IOException;
import java.io.InputStream;
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
                                    user_id BIGINT NOT NULL,
                                    account_number VARCHAR(32) UNIQUE NOT NULL,
                                    balance DECIMAL(15,2) NOT NULL,
                                    FOREIGN KEY (user_id) REFERENCES users(id)
                                );
                                CREATE TABLE IF NOT EXISTS transactions (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    account_id BIGINT NOT NULL,
                                    type VARCHAR(16) NOT NULL,
                                    amount DECIMAL(15,2) NOT NULL,
                                    occurred_at TIMESTAMP NOT NULL,
                                    description VARCHAR(255),
                                    FOREIGN KEY (account_id) REFERENCES accounts(id)
                                );
                                CREATE TABLE IF NOT EXISTS beneficiaries (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT NOT NULL,
                                    name VARCHAR(128) NOT NULL,
                                    account_number VARCHAR(32) NOT NULL,
                                    bank VARCHAR(128) NOT NULL,
                                    FOREIGN KEY (user_id) REFERENCES users(id)
                                );
                                """;
                try (var conn = ds.getConnection(); var stmt = conn.createStatement()) {
                        stmt.execute(ddl);
                        log.info("Ensured schema is present");
                } catch (Exception e) {
                        throw new IllegalStateException("Failed to initialize schema", e);
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
