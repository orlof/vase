package org.megastage.vase;

import com.esotericsoftware.minlog.Log;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DatabaseConnection implements AutoCloseable {
    static final String driver 	= "org.postgresql.Driver";
    static final String url 	= "jdbc:postgresql://localhost/mfactory";
    static final String user 	= "mfactory";
    static final String pass 	= "";

    public abstract Connection getConnection() throws SQLException;
    public abstract void close();
    public abstract <T extends DaoObject> T create(T dao) throws SQLException;
    public abstract <T extends DaoObject> T read(Class<T> clazz, Object key) throws SQLException;
    public abstract <T extends DaoObject> List<T> readAll(Class<T> clazz) throws SQLException;
    public abstract <T extends DaoObject> boolean update(T item) throws SQLException;
    public abstract <T extends DaoObject> boolean delete(Class<T> clazz, Object key) throws SQLException;

    public <T extends DaoObject> T safeCreate(T dao) {
        try { return create(dao); }
        catch(SQLException e) {
            Log.error("DB", "safeCreate failed", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> T safeRead(Class<T> clazz, Object key) {
        try { return read(clazz, key); }
        catch(SQLException e) {
            Log.error("DB", "safeRead failed", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> List<T> safeReadAll(Class<T> clazz) {
        try { return readAll(clazz); }
        catch(SQLException e) {
            Log.error("DB", "safeReadAll failed", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> boolean safeUpdate(T item) {
        try { return update(item); }
        catch(SQLException e) {
            Log.error("DB", "safeUpdate failed", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> boolean safeDelete(Class<T> clazz, Object key) {
        try { return delete(clazz, key); }
        catch(SQLException e) {
            Log.error("DB", "safeDelete failed", e);
            throw new RuntimeException(e);
        }
    }


    public boolean delete(DaoObject dao) throws SQLException {
        return delete(dao.getClass(), DaoObject.getKeyValue(dao));
    }

    public boolean safeDelete(DaoObject dao) {
        try { return delete(dao); }
        catch(SQLException e) {
            Log.error("DB", "safeDelete failed", e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends DaoObject> String getTableName(Class<T> clazz) {
        String tableName = clazz.getSimpleName();

        SqlTableName anno = clazz.getAnnotation(SqlTableName.class);
        if(anno != null) {
            tableName = anno.value();
        }

        return tableName;
    }

    <T extends DaoObject> DaoStatement create_READ(Connection conn, Class<T> clazz) throws SQLException {
        Field keyField = DaoObject.getKeyField(clazz);

        String key = keyField.getName();
        String value = decorateColumn(keyField.getType(), key);

        String sql = String.format("SELECT * FROM %s WHERE %s=%s", getTableName(clazz), key, value);

        return new DaoStatement(conn, sql);
    }

    <T extends DaoObject> DaoStatement create_READ_ALL(Connection conn, Class<T> clazz) throws SQLException {
        String sql = "SELECT * FROM " + getTableName(clazz);
        return new DaoStatement(conn, sql);
    }

    <T extends DaoObject> DaoStatement create_CREATE(Connection conn, Class<T> clazz) throws SQLException {
        List<String> cols = Arrays.stream(DaoObject.getFields(clazz))
                .filter(field -> field.getAnnotation(SqlSerial.class) == null)
                .map(Field::getName)
                .collect(Collectors.toList());

        List<String> values = Arrays.stream(DaoObject.getFields(clazz))
                .filter(field -> field.getAnnotation(SqlSerial.class) == null)
                .map(f -> decorateColumn(f.getType(), f.getName()))
                .collect(Collectors.toList());

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING *",
                getTableName(clazz), String.join(", ", cols), String.join(", ", values));

        return new DaoStatement(conn, sql);
    }

    <T extends DaoObject> DaoStatement create_UPDATE(Connection conn, Class<T> clazz) throws SQLException {
        Field keyField = DaoObject.getKeyField(clazz);

        String key = keyField.getName();
        String value = decorateColumn(keyField.getType(), key);

        List<String> cols = Arrays.stream(DaoObject.getFields(clazz))
                .filter(f -> f != keyField)
                .map(f -> String.format("%s=%s", f.getName(), decorateColumn(f.getType(), f.getName())))
                .collect(Collectors.toList());

        String sql = String.format("UPDATE %s SET %s WHERE %s=%s",
                getTableName(clazz), String.join(", ", cols), key, value);

        return new DaoStatement(conn, sql);
    }

    <T extends DaoObject> DaoStatement create_DELETE(Connection conn, Class<T> clazz) throws SQLException {
        Field keyField = DaoObject.getKeyField(clazz);

        String key = keyField.getName();
        String value = decorateColumn(keyField.getType(), key);

        String sql = String.format("DELETE FROM %s WHERE %s=%s", getTableName(clazz), key, value);

        return new DaoStatement(conn, sql);
    }

    public static String decorateColumn(Class clazz, String col) {
        if (clazz.isEnum()) {
            SqlTableName anno = (SqlTableName) clazz.getAnnotation(SqlTableName.class);
            String tableName = anno == null ? clazz.getSimpleName() : anno.value();
            return ":" + col + "::" + tableName;
        }

        return ":" + col;
    }
}
