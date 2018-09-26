package org.megastage.vase;

import java.sql.*;
import java.util.*;

/**
 * This class wraps around a {@link PreparedStatement} and allows the programmer to set parameters by name instead
 * of by index.  This eliminates any confusion as to which parameter index represents what.  This also means that
 * rearranging the SQL statement or adding a parameter doesn't involve renumbering your indices.
 * Code such as this:
 *
 * Connection con=getConnection();
 * String query="select * from my_table where name=? or address=?";
 * PreparedStatement p=con.prepareStatement(query);
 * p.setString(1, "bob");
 * p.setString(2, "123 terrace ct");
 * ResultSet rs=p.executeQuery();
 *
 * can be replaced with:
 *
 * Connection con=getConnection();
 * String query="select * from my_table where name=:name or address=:address";
 * NamedParameterStatement p=new NamedParameterStatement(con, query);
 * p.setString("name", "bob");
 * p.setString("address", "123 terrace ct");
 * ResultSet rs=p.executeQuery();
 */
public class NamedParameterStatement implements AutoCloseable {
    /** The statement this object is wrapping. */
    protected final PreparedStatement statement;

    /** Maps parameter names to arrays of ints which are the parameter indices. */
    private final Map<String, int[]> indexMap = new HashMap<>();

    /**
     * Creates a NamedParameterStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        String parsedQuery = parse(query, indexMap);
        statement = connection.prepareStatement(parsedQuery);
    }

    /**
     * Parses a query with named parameters.  The parameter-index mappings are put into the map, and the
     * parsed query is returned.
     * @param query    query to parse
     * @return the parsed query
     */
    public static String parse(String query, Map<String, int[]> indexMap) {
        HashMap<String, List<Integer>> paramMap = new HashMap<>();

        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index=1;

        for(int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if(inSingleQuote) {
                if(c == '\'') {
                    inSingleQuote=false;
                }
            } else if(inDoubleQuote) {
                if(c == '"') {
                    inDoubleQuote=false;
                }
            } else {
                if(c == '\'') {
                    inSingleQuote=true;
                } else if(c == '"') {
                    inDoubleQuote=true;
                } else if(isValidPrefix(query, i, c)) {
                    int j = i + 2;
                    while(j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i+1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    paramMap.computeIfAbsent(name, k -> new LinkedList<>()).add(index++);
                }
            }
            parsedQuery.append(c);
        }

        for (Map.Entry<String, List<Integer>> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            List<Integer> list = entry.getValue();
            int[] indexes = new int[list.size()];

            Iterator<Integer> iter = list.iterator();
            for (int i=0; iter.hasNext(); i++) {
                indexes[i] = iter.next();
            }

            indexMap.put(key, indexes);
        }

        return parsedQuery.toString();
    }

    private static boolean isValidPrefix(String query, int i, char c) {
        int length = query.length();
        return c == ':'
                && (i+1 < length && Character.isJavaIdentifierStart(query.charAt(i+1)))
                && (i > 0 && query.charAt(i-1) != ':');
    }

    /**
     * Returns the indexes for a parameter.
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private int[] getIndexes(String name) {
        int[] indexes = indexMap.get(name);
        if(indexes==null) {
            throw new IllegalArgumentException("Parameter not found: " + name);
        }
        return indexes;
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, java.lang.Object)
     */
    public void setObject(String name, Object value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setObject(index, value);
        }
    }

    public void setObjectEx(String name, Object value) throws SQLException {
        try {
            setObject(name, value);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    public void setString(String name, String value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setString(index, value);
        }
    }

    public void setStringEx(String name, String value) throws SQLException {
        try {
            setString(name, value);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, int value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setInt(index, value);
        }
    }

    public void setIntEx(String name, int value) throws SQLException {
        try {
            setInt(name, value);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setLong(String name, long value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setLong(index, value);
        }
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    public void setTimestamp(String name, Timestamp value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setTimestamp(index, value);
        }
    }

    public void setTimestampEx(String name, Timestamp value) throws SQLException {
        try {
            setTimestamp(name, value);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setBoolean(String name, boolean value) throws SQLException {
        for(int index : getIndexes(name)) {
            statement.setBoolean(index, value);
        }
    }

    public void setBooleanEx(String name, boolean value) throws SQLException {
        try {
            for(int index : getIndexes(name)) {
                statement.setBoolean(index, value);
            }
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Returns the underlying statement.
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }

    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return statement.execute();
    }


    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
     statement;
     * or an SQL statement that returns nothing, such as a DDL statement.
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    /**
     * Closes the statement.
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void close() throws SQLException {
        statement.close();
    }

    /**
     * Adds the current set of parameters as a batch entry.
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException {
        statement.addBatch();
    }

    /**
     * Executes all of the batched statements.
     *
     * See {@link Statement#executeBatch()} for details.
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }
}


