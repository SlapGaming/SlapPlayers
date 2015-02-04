package nl.stoux.SlapPlayers.Control;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nl.stoux.SlapPlayers.Model.Name;
import nl.stoux.SlapPlayers.Model.NameImpl;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.Model.ProfileImpl;
import nl.stoux.SlapPlayers.SQL.Dao;
import nl.stoux.SlapPlayers.SQL.DaoControl;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.*;
import java.util.*;

/**
 * Created by Stoux on 05/01/2015.
 */
public class UUIDControlImpl implements UUIDControl, Listener {

    private SlapPlayers plugin;

    //Map which has the String version of a player's UUID to their UUID Profile
    private HashMap<String, ProfileImpl> uuidToProfile;

    //Map which has the User ID (in the database) to their UUID Profile
    private HashMap<Integer, ProfileImpl> idToProfile;

    //Multimap which has a playername lead to one or more User IDs (Database)
    private Multimap<String, Integer> playerToIDs;

    //Set which contains UUID scurrently being added
    private HashSet<String> uuidsBeingAdded;

    public UUIDControlImpl(SlapPlayers plugin) {
        this.plugin = plugin;
        uuidToProfile = new HashMap<>();
        idToProfile = new HashMap<>();
        playerToIDs = ArrayListMultimap.create();
        uuidsBeingAdded = new HashSet<>();

        //Load all current entries
        loadFromDatabase();

        //Register this controller with the EventListener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Load all entries currently in the database. */
    private void loadFromDatabase() {
        Connection con = SlapPlayers.getSQLPool().getConnection();
        try {
            //Create the DAO
            Dao<ProfileImpl> profileDAO = DaoControl.createDAO(ProfileImpl.class);
            //Get all users
            for (ProfileImpl profile : profileDAO.selectAll()) {
                uuidToProfile.put(profile.getUUIDString(), profile);
                idToProfile.put(profile.getID(), profile);
            }
            profileDAO.destroy();

            //Create the DAO
            Dao<NameImpl> nameDAO = DaoControl.createDAO(NameImpl.class);
            //Get all names, ordered by ID
            for (NameImpl name : nameDAO.selectRaw("ORDER BY `known_since` DESC")) {
                //Get the profile
                ProfileImpl profile = idToProfile.get(name.getProfileID());

                //Add the name to the profile & the name -> ids map
                profile.addName(name);
                playerToIDs.put(name.getPlayername().toLowerCase(), name.getProfileID());
            }

            //Log
            Log.info("[UUIDControl] Loaded " + idToProfile.size() + " UUID Profiles.");
            Log.info("[UUIDControl] Found " + playerToIDs.size() + " different Playernames.");
        } catch (SQLException e) {
            Log.severe("[UUIDControl] Failed to get all users. SlapHomebrew cannot function without this. Shutting down.");
            Log.severe("[UUIDControl] Exception: " + e.getMessage());
        } finally {
            SlapPlayers.getSQLPool().returnConnection(con);
        }
    }

    /**
     * This function will add a new UUID to the database or add a new Playername to a UUID.
     * @param event The login event
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        final Player p = event.getPlayer();
        final String UUID = p.getUniqueId().toString();
        final long loginTime = System.currentTimeMillis();
        final String playername = p.getName();
        //Check if player is already known
        if (uuidToProfile.containsKey(UUID)) {
            //UUID is already known, check playername.
            ProfileImpl profile = uuidToProfile.get(UUID);
            Name latestName = profile.getNames().get(0);
            if (!latestName.getPlayername().equals(playername)) {
                //Has a new name
                final NameImpl newName = new NameImpl(profile.getID(), playername, loginTime);
                //Add it to the maps
                playerToIDs.put(p.getName().toLowerCase(), profile.getID());

                //Store the new name
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        insert(newName);
                    } catch (SQLException e) {
                        Log.severe("Failed to insert name: " + profile.getUUIDString() + " | N: " + newName.getPlayername() + " | E: " + e.getMessage());
                    }
                });
            }
        } else {
            //UUID is not known yet
            uuidsBeingAdded.add(UUID);
            Log.info("New UUID detected (" + UUID + ")! Adding to database.");

            //Add to the database
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    //Create the Entry & DAO
                    ProfileImpl profile = new ProfileImpl(UUID);
                    insert(profile);

                    //Get the ID
                    int id = profile.getID();
                    System.out.println("ID: " + id);

                    //Create the name profile
                    NameImpl name = new NameImpl(id, playername, loginTime);
                    profile.addName(name);

                    //Put them in the maps
                    synchronized (uuidToProfile) {
                        uuidToProfile.put(UUID, profile);
                    }
                    synchronized (idToProfile) {
                        idToProfile.put(id, profile);
                    }
                    synchronized (playerToIDs) {
                        playerToIDs.put(playername.toLowerCase(), id);
                    }
                    synchronized (uuidsBeingAdded) {
                        uuidsBeingAdded.remove(UUID);
                    }

                    //Log
                    Log.info("New UUID Profile added. ID:" + id + " | UUID: " + UUID + " | P: " + playername);

                    //Insert the name
                    insert(name);
                } catch (SQLException e) {
                    Log.severe("Failed to register new UUID Profile (UUID: " + UUID + ").");
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public Profile getProfile(UUID uuid) {
        return getProfile(uuid.toString());
    }

    @Override
    public Profile getProfile(String uuid) {
        return uuidToProfile.get(uuid);
    }

    @Override
    public Profile getProfile(int profileID) {
        return idToProfile.get(profileID);
    }

    @Override
    public Profile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    @Override
    public Collection<Integer> getUserIDs(String playername) {
        return playerToIDs.get(playername.toLowerCase());
    }

    /**
     * Insert an entry into the DB
     * @param object The entry
     * @param <T> The class of the entry. Needs to be annotated with @Table
     * @throws SQLException if failed
     */
    private <T> void insert(T object) throws SQLException {
        Dao<T> dao = (Dao<T>) DaoControl.createDAO(object.getClass());
        dao.insert(object);
        dao.destroy();
    }

}
