package nl.stoux.SlapPlayers.Control;

import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by Stoux on 05/01/2015.
 */
public interface UUIDControl {

    /**
     * Get a player's UUIDProfile based on their UUID
     * @param UUID the UUID
     * @return the profile or null
     */
    public Profile getProfile(UUID uuid);

    /**
     * Get a player's UUIDProfile based on their UUID
     * @param uuid the UUID as String
     * @return the profile or null
     */
    public Profile getProfile(String uuid);

    /**
     * Get a player's UUIDProfile based on their profile ID as specified in the database
     * @param profileID the profile ID
     * @return the profile or null
     */
    public Profile getProfile(int profileID);

    /**
     * Get a player's UUIDProfile
     * @param player the player
     * @return the profile or null
     */
    public Profile getProfile(Player player);

    /**
     * Get the UserIDs that have ever used this playername
     * @param playername The name of the player
     * @return a collection of UserIDs or null if the name has never been used
     */
    public Collection<Integer> getUserIDs(String playername);



}
