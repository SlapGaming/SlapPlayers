package nl.stoux.SlapPlayers.SQL;

import nl.stoux.SlapPlayers.SQL.Model.ColumnField;
import nl.stoux.SlapPlayers.SQL.Model.FullTable;
import nl.stoux.SlapPlayers.SQL.Model.SqlFunction;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.Log;
import nl.stoux.SlapPlayers.Util.SUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Stoux on 23/01/2015.
 */
public class DaoImpl<T extends Object> implements Dao<T> {

    private Connection connection;
    private FullTable table;
    private Class<T> tClass;

    public DaoImpl(Connection connection, FullTable table, Class<T> tClass) {
        this.connection = connection;
        this.table = table;
        this.tClass = tClass;
    }

    @Override
    public List<T> selectAll() throws SQLException {
        return parseResults(connection.createStatement().executeQuery(buildSelectSQL()));
    }

    @Override
    public <ValueClass> List<T> selectWhere(String column, ValueClass value) throws SQLException {
        //Prepare the statement
        String sql = buildSelectSQL() + " WHERE `" + column + "` = ?;";
        PreparedStatement prep = connection.prepareStatement(sql);
        prep.setObject(1, value);

        //Execute it
        return parseResults(prep.executeQuery());
    }

    @Override
    public List<T> selectRaw(String raw) throws SQLException {
        //Prepare the statement
        String sql = buildSelectSQL() + " " + raw;
        return parseResults(connection.createStatement().executeQuery(sql));
    }

    @Override
    public PreparedStatement createSelectPrepare(String additionalSQL) throws SQLException {
        return connection.prepareStatement(buildSelectSQL() + " " + additionalSQL);
    }

    @Override
    public List<T> querySelectPrepare(PreparedStatement preparedStatement) throws SQLException {
        return parseResults(preparedStatement.executeQuery());
    }

    @Override
    public void insert(T entry) throws SQLException {
        //Filter out auto increment fields
        List<ColumnField> filteredFields = table.getFields().stream().filter(f -> !f.isAutoIncrement()).collect(Collectors.toList());
        boolean hasAutoIncrement = filteredFields.size() != table.getFields().size();

        //Create the SQL query
        String fieldsSQL = SUtil.combineToString(filteredFields, ", ", f -> "`" + f.getName() + "`");
        String qmSQL = SUtil.combineToString(filteredFields, ", ", f -> "?");
        String sql = "INSERT INTO `" + table.getName() + "` (" + fieldsSQL + ") VALUES (" + qmSQL + ")";

        //Prepare the statement
        PreparedStatement prep = (hasAutoIncrement ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql));

        //Set values
        for (int i = 0; i < filteredFields.size(); i++) {
            Object value;
            try {
                value = filteredFields.get(i).getField().get(entry);
                if (value == null) {
                    prep.setNull(i + 1, Types.VARCHAR); //TODO
                } else {
                    prep.setObject(i + 1, value);
                }
            } catch (IllegalAccessException e) {
                Log.warn("Failed to access field");
            }
        }

        //Execute
        prep.executeUpdate();
        if (hasAutoIncrement) {
            //Get keys
            ResultSet keys = prep.getGeneratedKeys();

            //Find the auto increment fields
            table.getFields().stream().filter(ColumnField::isAutoIncrement).forEach(cf -> {
                try {
                    keys.next();
                    //Get the key
                    int key = keys.getInt(1);

                    //Insert that into the field
                    try {
                        cf.getField().setInt(entry, key);
                    } catch (Exception e) {
                        Log.severe("Failed to set key in entry: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Log.severe("Failed to find auto generated key: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Build the Select SQL
     * @return the sql
     */
    private String buildSelectSQL() {
        return "SELECT " + SUtil.combineToString(table.getFields(), ", ", f -> "`" + f.getName() + "`") + " FROM `" + table.getName() + "`";
    }

    /**
     * Parse the results
     * @param results The results
     * @return list with results
     * @throws SQLException
     */
    private List<T> parseResults(ResultSet results) throws SQLException {
        List<T> targetList = new ArrayList<>();
        while(results.next()) {
            T result = parseResult(results);
            if (result != null) {
                targetList.add(result);
            }
        }
        return targetList;
    }

    /**
     * Parse a single result
     * @param set The set with results
     * @return The resulting object or null
     */
    private T parseResult(ResultSet set) {
        try {
            //Create a new Instance of the object
            T object = tClass.newInstance();

            //Loop through fields (& results)
            List<ColumnField> fields = table.getFields();
            for (int i = 0; i < fields.size(); i++) {
                //Get the field
                ColumnField field = fields.get(i);

                //Get the result
                Object x = getMethod(set, field.getType()).apply(i + 1);

                //Set the result in the object
                field.getField().set(object, x);
            }

            //Return the object
            return object;
        } catch (Exception e) {
            Log.warn("Failed to parse result (" + table.getName() + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the method for a certain class
     * @param set The result set
     * @param clazz The class of the object that is to be received
     * @return the SqlFunction
     */
    private SqlFunction<Integer, Object> getMethod(final ResultSet set, Class<?> clazz) {
        if (clazz.equals(String.class)) {
            return set::getString;
        } else if (clazz.equals(Integer.class)) {
            return set::getInt;
        } else if (clazz.equals(Long.class)) {
           return set::getLong;
        } else if (clazz.equals(Double.class)) {
            return set::getDouble;
        } else if (clazz.equals(Boolean.class)) {
            return set::getBoolean;
        } else {
            return set::getObject;
        }
    }

    @Override
    public void destroy() {
        SlapPlayers.getSQLPool().returnConnection(connection);
    }
}
