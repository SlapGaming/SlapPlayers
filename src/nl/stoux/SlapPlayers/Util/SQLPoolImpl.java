package nl.stoux.SlapPlayers.Util;

/**
 * Created by Stoux on 05/01/2015.
 */

import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * SQLPool that contains available SQL connections
 */
public class SQLPoolImpl implements SQLPool {

    private SlapPlayers plugin;

    private ArrayList<Connection> connections; //Connections

    private static boolean setup;

    private String host;
    private int port;
    private String db;
    private String user;
    private String password;

    /**
     * Create a new SQL Pool
     */
    public SQLPoolImpl(SlapPlayers plugin, String host, int port, String db, String user, String password) {
        this.plugin = plugin;
        //SQL Login data
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;

        //Create the connections
        connections = new ArrayList<>();
        for (int x = 0; x < 5; x++) { //Create connections
            Connection con = createConnection();
            if (con == null) {
                shutdown();
                setup = false;
                return;
            }
            connections.add(con);
        }
        startPinging(); //Start pinging
        setup = true;
    }

    /**
     * Check if the SQLPool setup correctly
     * @return is setup correct
     */
    public static boolean isSetup() {
        return setup;
    }

    /**
     * Start pinging the connections
     */
    private void startPinging() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                HashSet<Connection> connectionsSet = new HashSet<>(connections); //Create new hashset of connections
                for (Connection connection : connectionsSet) { //Loop thru connections
                    boolean stillInConnections = false;
                    synchronized (connections) { //Check if connection still in the array
                        if (connections.contains(connection)) {
                            stillInConnections = true;
                        }
                    }
                    if (stillInConnections) { //If still in array
                        try { //Try to ping
                            connection.isValid(5);
                        } catch (SQLException e) { //If not valid anymore
                            synchronized (connections) { //Remove from array
                                connections.remove(connection);
                            }
                            closeConnection(connection); //Close the connection
                        }
                    }
                }
            }
        }, 17000, 17000);
    }

    /**
     * Create a new connection with the database
     * @return The connection or null if failed
     */
    private Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get an available MySQL Connection which is not in use
     * WARNING: Make sure to return the connection when done.
     * @return a connection
     */
    @Override
    public Connection getConnection() {
        boolean createNewConnection = false;
        Connection returnConnection = null;
        synchronized (connections) { //Lock the arraylist
            if (connections.isEmpty()) { //Check if any connections available
                createNewConnection = true; //If not, create new one
            } else {
                returnConnection = connections.get(0); //Get first available one
                connections.remove(0); //And remove from list
            }
        }
        if (createNewConnection) { //If new connection needed
            returnConnection = createConnection();
        }
        return returnConnection;
    }

    /**
     * Return a connection to the SQLPool
     */
    @Override
    public void returnConnection(Connection connection) {

        boolean closeConnection = false;
        synchronized (connections) {
            if (connections.size() >= 5) { //Close the returned connection if there are already 5 connections in the pool.
                closeConnection = true;
            } else {
                connections.add(connection);
            }
        }
        if (closeConnection) {
            closeConnection(connection);
        }
    }

    /**
     * Try to close all the connections
     */
    public void shutdown() {
        for (Connection connection : connections) { //Try closing all connections
            closeConnection(connection);
        }
    }

    /**
     * Close a connection
     * @param connection
     */
    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            //Failed to close, not that big of a deal
        }
    }
}

