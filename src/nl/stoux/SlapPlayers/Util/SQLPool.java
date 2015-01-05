package nl.stoux.SlapPlayers.Util;

import java.sql.Connection;

/**
 * Created by Stoux on 05/01/2015.
 */
public interface SQLPool {

    /**
     * Get a connection
     * @return the connection
     */
    public Connection getConnection();

    /** Return a connection to the SQLPool */
    public void returnConnection(Connection connection);

}
