package nl.stoux.SlapPlayers;

import nl.stoux.SlapPlayers.Control.UUIDControl;
import nl.stoux.SlapPlayers.Control.UUIDControlImpl;
import nl.stoux.SlapPlayers.Model.NameImpl;
import nl.stoux.SlapPlayers.SQL.DaoControl;
import nl.stoux.SlapPlayers.Util.Log;
import nl.stoux.SlapPlayers.Util.SQLPool;
import nl.stoux.SlapPlayers.Util.SQLPoolImpl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;

/**
 * Created by Stoux on 05/01/2015.
 */
public class SlapPlayers extends JavaPlugin implements Listener {

    //Singleton
    private static SlapPlayers instance;

    /** The SQLPool */
    private SQLPoolImpl sqlPool;

    /** The UUID Controller */
    private UUIDControlImpl uuidControl;

    @Override
    public void onEnable() {
        super.onEnable();

        //Initialze statics
        instance = this;
        Log.intialize(getLogger());

        getServer().getPluginManager().registerEvents(this, this);

        //Create SQL Pool
        FileConfiguration config = getConfig();
        String host = config.getString("sql.host");
        int port = config.getInt("sql.port");
        String db = config.getString("sql.db");
        String user = config.getString("sql.user");
        String pass = config.getString("sql.password");
        sqlPool = new SQLPoolImpl(this, host, port, db, user, pass);

        //Register with DaoControl
        DaoControl.registerTables(getClass().getPackage().toString());

        //Create the UUIDController
        uuidControl = new UUIDControlImpl(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        //Shut down the SQLPool
        sqlPool.shutdown();
    }

    /**
     * Get the SQLPool
     * @return the SQLPool
     */
    public static SQLPool getSQLPool() {
        return instance.sqlPool;
    }

    /**
     * Get the UUIDController
     * @return the UUID controller
     */
    public static UUIDControl getUUIDController() {
        return instance.uuidControl;
    }

}
