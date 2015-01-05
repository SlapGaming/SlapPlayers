package nl.stoux.SlapPlayers.Model;

/**
 * Created by Stoux on 05/01/2015.
 */
public class NameImpl implements Name {

    private int profileID;
    private String playername;
    private Long knownSince;

    public NameImpl(int profileID, String playername, long knownSince) {
        this.profileID = profileID;
        this.playername = playername;
        this.knownSince = knownSince;
    }

    @Override
    public int getProfileID() {
        return profileID;
    }

    @Override
    public long getKnownSince() {
        return knownSince;
    }

    @Override
    public String getPlayername() {
        return playername;
    }

    @Override
    public int compareTo(Name o) {
        if (o instanceof NameImpl) {
            return ((NameImpl) o).knownSince.compareTo(knownSince);
        } else {
            return Long.valueOf(o.getKnownSince()).compareTo(knownSince);
        }
    }

    public void insertInDatabase() {

    }
}
