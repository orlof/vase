package org.megastage.vase;

import com.esotericsoftware.minlog.Log;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DatabaseConnectionPool extends DatabaseConnection {
    private BoneCP connectionPool;

    DatabaseConnectionPool() throws ClassNotFoundException, SQLException {
        Log.info("Init DatabaseConnectionPool...");
        Class.forName(driver);

        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(1);
        config.setDefaultAutoCommit(true);

        connectionPool = new BoneCP(config);
        Log.info("Done.");
    }

    @Override
    public void close() {
        Log.info("Close DatabaseConnectionPool...");
        connectionPool.shutdown();
        Log.info("Done.");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public <T extends DaoObject> T create(T dao) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_CREATE(conn, dao.getClass())) {
                return stmt.execute_CREATE(dao);
            }
        }
    }

    @Override
    public <T extends DaoObject> T read(Class<T> clazz, Object key) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_READ(conn, clazz)) {
                return stmt.execute_READ(clazz, key);
            }
        }
    }

    @Override
    public <T extends DaoObject> List<T> readAll(Class<T> clazz) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_READ_ALL(conn, clazz)) {
                return stmt.execute_READ_ALL(clazz);
            }
        }
    }

    @Override
    public <T extends DaoObject> boolean update(T item) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_UPDATE(conn, item.getClass())) {
                return stmt.execute_UPDATE(item);
            }
        }
    }

    @Override
    public <T extends DaoObject> boolean delete(Class<T> clazz, Object key) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_DELETE(conn, clazz)) {
                return stmt.execute_DELETE(clazz, key);
            }
        }
    }
}
