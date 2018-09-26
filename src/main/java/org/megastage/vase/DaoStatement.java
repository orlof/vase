package org.megastage.vase;

import com.esotericsoftware.minlog.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class DaoStatement extends NamedParameterStatement {
    protected ResultSet rs;

    public DaoStatement(Connection connection, String query) throws SQLException {
        super(connection, query);
    }

    /**
     * Returns the underlying result set.
     * @return the result set
     */
    public ResultSet getResultSet() {
        return rs;
    }


    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        boolean result = statement.execute();
        rs = statement.getResultSet();
        return result;
    }

    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        rs = statement.executeQuery();
        return rs;
    }

    public boolean next() throws SQLException {
        return rs.next();
    }

    public <T extends DaoObject> T execute_READ(Class<T> clazz, Object keyValue) throws SQLException {
        Field keyField = DaoObject.getKeyField(clazz);

        if (Enum.class.isAssignableFrom(keyField.getType())) {
            setString(keyField.getName(), String.valueOf(keyValue));
        } else {
            setObject(keyField.getName(), keyValue);
        }
        return execute_READ(clazz);
    }

    public <T extends DaoObject> T execute_READ(Class<T> clazz) throws SQLException {
        try {
            try(ResultSet rs = executeQuery()) {
                if(rs.next()) {
                    T item = setValuesTo(clazz.newInstance());
                    if(Log.DEBUG) Log.debug("VASE/READ", item.toString());
                    return item;
                }
                if(Log.DEBUG) Log.warn("VASE/READ", "null");
                return null;
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Log.error("VASE/READ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> List<T> execute_READ_ALL(Class<T> clazz) throws SQLException {
        ArrayList<T> result = new ArrayList<>();
        try(ResultSet rs = executeQuery()) {
            while(rs.next()) {
                T item = setValuesTo(clazz.newInstance());
                if(Log.DEBUG) Log.debug("VASE/READ_ALL", item.toString());
                result.add(item);
            }
            return result;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Log.error("VASE/READ_ALL", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public <T extends DaoObject> T execute_CREATE(T item) throws SQLException {
        setValuesFrom(item);
        try (ResultSet rs = executeQuery()) {
            if(rs.next()) {
                setValuesTo(item);
                if(Log.DEBUG) Log.debug("VASE/CREATE", item.toString());

            } else {
                Log.error("VASE/CREATE", "null");
            }
        }
        return item;
    }

    public <T extends DaoObject> boolean execute_UPDATE(T item) throws SQLException {
        setValuesFrom(item);
        int rows = executeUpdate();

        if(rows == 1) {
            if(Log.DEBUG) Log.debug("DB/UPDATE", String.format("%s -> %d rows", item.toString(), rows));
            return true;
        }

        Log.error("DB/UPDATE", String.format("%s -> %d rows", item.toString(), rows));
        return false;
    }

    public <T extends DaoObject> boolean execute_DELETE(Class<T> clazz, Object keyValue) throws SQLException {
        Field keyField = DaoObject.getKeyField(clazz);

        if (Enum.class.isAssignableFrom(keyField.getType())) {
            setString(keyField.getName(), String.valueOf(keyValue));
        } else {
            setObject(keyField.getName(), keyValue);
        }

        int rows =  executeUpdate();

        if(rows == 1) {
            if(Log.DEBUG) Log.debug("DB/DELETE", String.format("%s(%s=%s) -> %d rows",
                    clazz.getSimpleName(), keyField.getName(), keyValue, rows));
            return true;
        }

        Log.error("DB/DELETE", String.format("%s(%s=%s) -> %d rows",
                clazz.getSimpleName(), keyField.getName(), keyValue, rows));
        return false;
    }

    public void setValuesFrom(DaoObject src) throws SQLException {
        for(Field f: DaoObject.getFields(src.getClass())) {
            try {
                if(Enum.class.isAssignableFrom(f.getType())) {
                    setStringEx(f.getName(), f.get(src).toString());
                } else {
                    setObjectEx(f.getName(), f.get(src));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends DaoObject> T setValuesTo(T dst) throws SQLException {
        for(Field f: DaoObject.getFields(dst.getClass())) {
            try {
                if(Enum.class.isAssignableFrom(f.getType())) {
                    try {
                        Method valueOf = f.getType().getMethod("valueOf", String.class);
                        f.set(dst, valueOf.invoke(null, rs.getString(f.getName())));
                    } catch ( ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                } else {
                    f.set(dst, rs.getObject(f.getName()));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return dst;
    }
}
