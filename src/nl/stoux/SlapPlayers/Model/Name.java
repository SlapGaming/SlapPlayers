package nl.stoux.SlapPlayers.Model;

/**
 * Created by Stoux on 05/01/2015.
 */
public interface Name extends Comparable<Name> {

    /**
     * Get the profile ID (as specified in the database) associated with this name
     * @return the ID
     */
    public int getProfileID();

    /**
     * Get the timestamp (in millis) since this username was first known
     * @return the timestamp
     */
    public long getKnownSince();

    /**
     * Get the playername being used in this profile
     * @return the playername
     */
    public String getPlayername();

}
