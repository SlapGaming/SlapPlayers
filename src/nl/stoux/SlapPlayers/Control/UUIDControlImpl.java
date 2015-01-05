package nl.stoux.SlapPlayers.Control;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nl.stoux.SlapPlayers.Model.Name;
import nl.stoux.SlapPlayers.Model.NameImpl;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.Model.ProfileImpl;
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
            //Get all 'Users'. UUID -> ID
            ResultSet users = con.createStatement().executeQuery("SELECT `user_id` as `ID`, `UUID` FROM `sh_user`;");
            while(users.next()) {
                //Get data
                int ID = users.getInt(1);
                String UUID = users.getString(2);

                //Create the profile
                ProfileImpl profile = new ProfileImpl(ID, UUID);
                //Add the profile to the maps
                uuidToProfile.put(UUID, profile);
                idToProfile.put(ID, profile);
            }

            int currentID = -1;
            ArrayList<Name> profiles = new ArrayList<>();
            //Get all names
            ResultSet names = con.createStatement().executeQuery("SELECT `user_id`, `name`, `known_since` FROM `sh_names` ORDER BY `user_id` ASC;");
            while(names.next()) {
                //Get data
                int ID = names.getInt(1);
                String name = names.getString(2);
                long knownSince = names.getLong(3);

                //Check if still adding to the same UUID Profile
                if (currentID != -1) {
                    if (currentID != ID) { //Otherwise store them
                        ProfileImpl uuidProfile = idToProfile.get(currentID);
                        uuidProfile.addNames(profiles);
                        profiles.clear();
                    }
                }

                //Set current ID
                currentID = ID;

                //Create the profile
                NameImpl nProfile = new NameImpl(ID, name, knownSince);
                profiles.add(nProfile);

                //Add to the map
                playerToIDs.put(name.toLowerCase(), ID);
            }

            //Store any remaining profiles
            if (currentID != -1) {
                ProfileImpl uuidProfile = idToProfile.get(currentID);
                uuidProfile.addNames(profiles);
            }

            //Clear profiles
            profiles = null;

            //Log
            Log.info("[UUIDControl] Loaded " + idToProfile.size() + " UUID Profiles.");
            Log.info("[UUIDControl] Found " + playerToIDs.size() + " different Playernames.");
        } catch (SQLException e) {
            Log.severe("[UUIDControl] Failed to get all users. SlapHomebrew cannot function without this. Shutting down.");
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
            if (!latestName.getPlayername().equalsIgnoreCase(playername)) {
                //Has a new name
                final NameImpl newName = new NameImpl(profile.getID(), playername, loginTime);
                //Add it to the maps
                playerToIDs.put(p.getName().toLowerCase(), profile.getID());

                //Store the new name

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        newName.insertInDatabase();
                    }
                });
            }
        } else {
            //UUID is not known yet
            uuidsBeingAdded.add(UUID);
            Log.info("New UUID detected (" + UUID + ")! Adding to database.");

            //Add to the database
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    Connection con = SlapPlayers.getSQLPool().getConnection();
                    try {
                        //Prepare the statement
                        PreparedStatement prep = con.prepareStatement("INSERT INTO `sh_user`(`UUID`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
                        //Set data
                        prep.setString(1, UUID);
                        //Execute & get keys
                        prep.executeUpdate();
                        ResultSet key = prep.getGeneratedKeys();
                        key.next();
                        int ID = key.getInt(1);

                        //Create the profile
                        ProfileImpl newProfile = new ProfileImpl(ID, UUID);
                        NameImpl newName = new NameImpl(ID, playername, loginTime);
                        newProfile.addName(newName);

                        //Put it in the maps
                        synchronized (uuidToProfile) {
                            uuidToProfile.put(UUID, newProfile);
                        }
                        synchronized (idToProfile) {
                            idToProfile.put(ID, newProfile);
                        }
                        synchronized (playerToIDs) {
                            playerToIDs.put(playername.toLowerCase(), ID);
                        }
                        synchronized (uuidsBeingAdded) {
                            uuidsBeingAdded.remove(UUID);
                        }

                        //Log
                        Log.info("New UUID Profile added. ID:" + ID + " | UUID: " + UUID + " | P: " + playername);

                        //Insert the name into the database
                        newName.insertInDatabase();
                    } catch (SQLException e) {
                        Log.severe("Failed to register new UUID Profile (UUID: " + UUID + ").");
                        e.printStackTrace();
                    } finally {
                        SlapPlayers.getSQLPool().returnConnection(con);
                    }
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


}
