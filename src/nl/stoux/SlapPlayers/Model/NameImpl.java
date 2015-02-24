package nl.stoux.SlapPlayers.Model;

import lombok.NoArgsConstructor;
import nl.stoux.SlapPlayers.SQL.Annotations.Column;
import nl.stoux.SlapPlayers.SQL.Annotations.Table;

/**
 * Created by Stoux on 05/01/2015.
 */
@Table("sh_names")
@NoArgsConstructor
public class NameImpl implements Name {

    @Column("user_id")
    private int profileID;
    @Column("name")
    private String playername;
    @Column("known_since")
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

}
