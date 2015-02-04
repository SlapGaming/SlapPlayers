package nl.stoux.SlapPlayers.SQL;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Stoux on 23/01/2015.
 */
public interface Dao<TableClass extends Object> {

    /**
     * Select all entries in the DB
     * @return The results
     * @throws SQLException
     */
    public List<TableClass> selectAll() throws SQLException;

    /**
     * Select all results with a certain value
     * @param column The columnname
     * @param value The value
     * @param <ValueClass> The class of the value
     * @return The results
     * @throws SQLException if failed
     */
    public <ValueClass extends Object> List<TableClass> selectWhere(String column, ValueClass value) throws SQLException;

    /**
     * Select with raw SQL added after 'SELECT ... FROM table '
     * @param raw Raw SQL (where/order/limit)
     * @return the results
     * @throws SQLException if failed
     */
    public List<TableClass> selectRaw(String raw) throws SQLException;

    /**
     * Create a prepared select statement
     * @param additionalSQL Additional sql after 'SELECT ... FROM table '
     * @return The prepared statement
     * @throws SQLException if failed
     */
    public PreparedStatement createSelectPrepare(String additionalSQL) throws SQLException;

    /**
     * Execute a prepared select statement
     * @param preparedStatement The statement
     * @return The list with results
     * @throws SQLException if failed
     */
    public List<TableClass> querySelectPrepare(PreparedStatement preparedStatement) throws SQLException;

    /**
     * Insert an entry into the DB
     * @param entry The entry
     * @throws SQLException if failed
     */
    public void insert(TableClass entry) throws SQLException;

    /** Destroy this DAO. This dao instance can't be used anymore after being called */
    public void destroy();


}
