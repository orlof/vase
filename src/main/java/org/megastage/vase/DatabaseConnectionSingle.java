package org.megastage.vase;

import com.esotericsoftware.minlog.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DatabaseConnectionSingle extends DatabaseConnection {
    public final Connection conn;

    private final HashMap<Class, HashMap<String, DaoStatement>> statements = new HashMap<>();

    public DatabaseConnectionSingle() throws SQLException {
        Log.info("Init DatabaseConnectionSingle...");

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);

        conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(true);

        Log.info("Done");
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        try {
            for(HashMap<String, DaoStatement> typeStmt: statements.values()) {
                for(DaoStatement stmt: typeStmt.values()) {
                    stmt.close();
                }
            }
            conn.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    @Override
    public <T extends DaoObject> T create(T dao) throws SQLException {
        if(!statements.containsKey(dao.getClass())) {
            statements.put(dao.getClass(), new HashMap<>());
        }
        HashMap<String, DaoStatement> typeStmt = statements.get(dao.getClass());

        if(!typeStmt.containsKey("CREATE")) {
            typeStmt.put("CREATE", create_CREATE(getConnection(), dao.getClass()));
        }
        DaoStatement stmt = typeStmt.get("CREATE");

        return stmt.execute_CREATE(dao);
    }

    @Override
    public <T extends DaoObject> T read(Class<T> clazz, Object key) throws SQLException {
        if(!statements.containsKey(clazz)) {
            statements.put(clazz, new HashMap<>());
        }
        HashMap<String, DaoStatement> typeStmt = statements.get(clazz);

        if(!typeStmt.containsKey("READ")) {
            typeStmt.put("READ", create_READ(getConnection(), clazz));
        }
        DaoStatement stmt = typeStmt.get("READ");
        return stmt.execute_READ(clazz, key);
    }

    @Override
    public <T extends DaoObject> List<T> readAll(Class<T> clazz) throws SQLException {
        if(!statements.containsKey(clazz)) {
            statements.put(clazz, new HashMap<>());
        }
        HashMap<String, DaoStatement> typeStmt = statements.get(clazz);

        if(!typeStmt.containsKey("READ_ALL")) {
            typeStmt.put("READ_ALL", create_READ_ALL(getConnection(), clazz));
        }
        DaoStatement stmt = typeStmt.get("READ_ALL");
        return stmt.execute_READ_ALL(clazz);
    }

    @Override
    public <T extends DaoObject> boolean update(T dao) throws SQLException {
        if(!statements.containsKey(dao.getClass())) {
            statements.put(dao.getClass(), new HashMap<>());
        }
        HashMap<String, DaoStatement> typeStmt = statements.get(dao.getClass());

        if(!typeStmt.containsKey("UPDATE")) {
            typeStmt.put("UPDATE", create_UPDATE(getConnection(), dao.getClass()));
        }
        DaoStatement stmt = typeStmt.get("UPDATE");

        return stmt.execute_UPDATE(dao);
    }

    @Override
    public <T extends DaoObject> boolean delete(Class<T> clazz, Object key) throws SQLException {
        if(!statements.containsKey(clazz)) {
            statements.put(clazz, new HashMap<>());
        }
        HashMap<String, DaoStatement> typeStmt = statements.get(clazz);

        if(!typeStmt.containsKey("DELETE")) {
            typeStmt.put("DELETE", create_DELETE(getConnection(), clazz));
        }
        DaoStatement stmt = typeStmt.get("DELETE");

        return stmt.execute_DELETE(clazz, key);
    }
}
