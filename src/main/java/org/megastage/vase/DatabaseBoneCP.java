package org.megastage.vase;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DatabaseBoneCP extends Database {
    private BoneCP connectionPool;

    DatabaseBoneCP(String driver, String url, String user, String pass) throws ClassNotFoundException, SQLException {
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
    }

    @Override
    public void close() {
        connectionPool.shutdown();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public <T> T create(T dao) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_CREATE(conn, dao.getClass())) {
                return stmt.execute_CREATE(dao);
            }
        }
    }

    @Override
    public <T> T read(Class<T> clazz, Object key) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_READ(conn, clazz)) {
                return stmt.execute_READ(clazz, key);
            }
        }
    }

    @Override
    public <T> List<T> readAll(Class<T> clazz) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_READ_ALL(conn, clazz)) {
                return stmt.execute_READ_ALL(clazz);
            }
        }
    }

    @Override
    public <T> boolean update(T item) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_UPDATE(conn, item.getClass())) {
                return stmt.execute_UPDATE(item);
            }
        }
    }

    @Override
    public <T> boolean delete(Class<T> clazz, Object key) throws SQLException {
        try(Connection conn = getConnection()) {
            try (DaoStatement stmt = create_DELETE(conn, clazz)) {
                return stmt.execute_DELETE(clazz, key);
            }
        }
    }
}
