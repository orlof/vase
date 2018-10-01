package org.megastage.vase;

import com.esotericsoftware.minlog.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DatabaseStatement extends Database {
    public final Connection conn;

    public DatabaseStatement(String url, String user, String pass) throws SQLException {
        conn = DriverManager.getConnection(url, user, pass);
        conn.setAutoCommit(true);
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    @Override
    public <T> T create(T dao) throws SQLException {
        PojoStatement stmt = create_CREATE(getConnection(), dao.getClass());
        return stmt.execute_CREATE(dao);
    }

    @Override
    public <T> T read(Class<T> clazz, Object key) throws SQLException {
        PojoStatement stmt = create_READ(getConnection(), clazz);
        return stmt.execute_READ(clazz, key);
    }

    @Override
    public <T> List<T> readAll(Class<T> clazz) throws SQLException {
        PojoStatement stmt = create_READ_ALL(getConnection(), clazz);
        return stmt.execute_READ_ALL(clazz);
    }

    @Override
    public <T> boolean update(T dao) throws SQLException {
        PojoStatement stmt = create_UPDATE(getConnection(), dao.getClass());
        return stmt.execute_UPDATE(dao);
    }

    @Override
    public <T> boolean delete(Class<T> clazz, Object key) throws SQLException {
        PojoStatement stmt = create_DELETE(getConnection(), clazz);
        return stmt.execute_DELETE(clazz, key);
    }
}
