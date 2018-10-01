package org.megastage.vase;

import com.esotericsoftware.minlog.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DatabasePreparedStatement extends Database {
    public final Connection conn;

    private final HashMap<Class, HashMap<String, PojoStatement>> statements = new HashMap<>();

    public DatabasePreparedStatement(String url, String user, String pass) throws SQLException {
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
            for(HashMap<String, PojoStatement> typeStmt: statements.values()) {
                for(PojoStatement stmt: typeStmt.values()) {
                    stmt.close();
                }
            }
            conn.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    @Override
    public <T> T create(T dao) throws SQLException {
        PojoStatement stmt = getDaoStatement(dao.getClass(), "CREATE");
        return stmt.execute_CREATE(dao);
    }

    @Override
    public <T> T read(Class<T> clazz, Object key) throws SQLException {
        PojoStatement stmt = getDaoStatement(clazz, "READ");
        return stmt.execute_READ(clazz, key);
    }

    @Override
    public <T> List<T> readAll(Class<T> clazz) throws SQLException {
        PojoStatement stmt = getDaoStatement(clazz, "READ_ALL");
        return stmt.execute_READ_ALL(clazz);
    }

    @Override
    public <T> boolean update(T dao) throws SQLException {
        PojoStatement stmt = getDaoStatement(dao.getClass(), "UPDATE");
        return stmt.execute_UPDATE(dao);
    }

    @Override
    public <T> boolean delete(Class<T> clazz, Object key) throws SQLException {
        PojoStatement stmt = getDaoStatement(clazz, "DELETE");
        return stmt.execute_DELETE(clazz, key);
    }

    private <T> PojoStatement getDaoStatement(Class<T> clazz, String stmtName) throws SQLException {
        if(!statements.containsKey(clazz)) {
            statements.put(clazz, new HashMap<>());
        }
        HashMap<String, PojoStatement> typeStmt = statements.get(clazz);

        if(!typeStmt.containsKey(stmtName)) {
            PojoStatement stmt = null;
            switch(stmtName) {
                case "CREATE":
                    stmt = create_CREATE(getConnection(), clazz);
                    break;
                case "READ":
                    stmt = create_READ(getConnection(), clazz);
                    break;
                case "READ_ALL":
                    stmt = create_READ_ALL(getConnection(), clazz);
                    break;
                case "UPDATE":
                    stmt = create_UPDATE(getConnection(), clazz);
                    break;
                case "DELETE":
                    stmt = create_DELETE(getConnection(), clazz);
                    break;
            }
            typeStmt.put(stmtName, stmt);
        }
        return typeStmt.get(stmtName);
    }
}
