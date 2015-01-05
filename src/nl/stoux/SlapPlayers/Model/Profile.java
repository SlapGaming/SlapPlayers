package nl.stoux.SlapPlayers.Model;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Stoux on 05/01/2015.
 */
public interface Profile {

    /**
     * Get the player's Database Profile ID
     * @return The ID
     */
    public int getID();

    /**
     * Get the player's Unique Identifier
     * @return the UUID as String
     */
    public String getUUIDString();

    /**
     * Get the player's Unique Identifier
     * @return the UUID
     */
    public UUID getUUID();

    /**
     * Get the list of used names.
     * It goes from new -> old, thus list[0] is the one currently being used.
     * @return The list with NameProfiles
     */
    public List<Name> getNames();

    /**
     * Get a player's current username
     * @return the username or null if no name is known
     */
    public String getCurrentName();

    /**
     * Get a player's current username
     * @param defaultName A default value that will be returned if no name is found
     * @return the username or the default name
     */
    public String getCurrentName(String defaultName);

    /**
     * Get the player that belongs to this UUIDProfile.
     * This will only return a result if that player is currently online.
     * @return The player or null
     */
    public Player getPlayer();

}
