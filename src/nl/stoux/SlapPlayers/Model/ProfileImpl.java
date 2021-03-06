package nl.stoux.SlapPlayers.Model;

import lombok.NoArgsConstructor;
import nl.stoux.SlapPlayers.SQL.Annotations.Column;
import nl.stoux.SlapPlayers.SQL.Annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Stoux on 05/01/2015.
 */
@Table("sh_user")
@NoArgsConstructor
public class ProfileImpl implements Profile {

    @Column(value = "user_id", autoIncrementID = true)
    private int id;

    @Column("UUID")
    private String uuid;

    //The list of names, in the order that they are being used.
    //names[0] will be the current one. names[1] will be their previous value, etc..
    private List<Name> names = new ArrayList<>();

    /**
     * Create a new UUID Profile
     * @param id The ID in the database
     * @param uuid The player's UUID supplied by Mojang
     */
    public ProfileImpl(int id, String uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public ProfileImpl(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getUUIDString() {
        return uuid;
    }

    @Override
    public UUID getUUID() {
        return UUID.fromString(uuid);
    }

    @Override
    public List<Name> getNames() {
        return names;
    }

    @Override
    public String getCurrentName() {
        if (names.isEmpty()) {
            return null;
        } else {
            return names.get(0).getPlayername();
        }
    }

    @Override
    public String getCurrentName(String defaultName) {
        String name = getCurrentName();
        return (name == null ? defaultName : name);
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(getUUID());
    }

    /**
     * Add a value to the Profile
     * @param name One or more names
     */
    public void addName(Name... name) {
        for (Name n : name) {
            names.add(n);
        }
        sortNames();
    }

    /**
     * Add a collection of names to the Profile
     * @param names the names
     */
    public void addNames(Collection<Name> names) {
        this.names.addAll(names);
        sortNames();
    }

    /** Sort the names */
    private void sortNames() {
        Collections.sort(names);
    }
}
