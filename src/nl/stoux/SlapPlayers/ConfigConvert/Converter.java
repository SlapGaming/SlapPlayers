package nl.stoux.SlapPlayers.ConfigConvert;

import nl.stoux.SlapPlayers.Control.UUIDControl;
import nl.stoux.SlapPlayers.Model.Name;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.Log;
import nl.stoux.SlapPlayers.Util.SUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Stoux on 06/02/2015.
 */
public class Converter {

    private CommandSender executor;

    private String pathToFile;
    private String filename;

    private File oldFile;
    private Configuration oldConfig;

    private File newFile;
    private YamlConfiguration newConfig;

    private String[] excludedKeys;

    private UUIDControl uuids;

    public Converter(CommandSender executor, String pathToFile, String filename, String... excludedKeys) {
        this.filename = filename;
        this.pathToFile = pathToFile;
        this.executor = executor;
        this.excludedKeys = excludedKeys;

        oldFile = createFile(filename);
        if (oldFile.exists()) {
            oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        }
        uuids = SlapPlayers.getUUIDController();
    }

    public void convert(){
        if (oldConfig.getKeys(true).isEmpty()) {
            msg("Empty config. Aborting.");
            return;
        }

        //Create the new Config
        newFile = createFile(filename + "-uuid");
        newConfig = YamlConfiguration.loadConfiguration(newFile);

        //Start the conversion
        convert(oldConfig, newConfig, true);

        //Save the new Config
        try {
            newConfig.save(newFile);
        } catch (Exception e) {
            msg("Failed to save file: " + e.getMessage());
        }
    }

    /**
     * Convert a section of a Configuration into UUID enabled
     * @param oldSection
     * @param newSection
     */
    private void convert(ConfigurationSection oldSection, ConfigurationSection newSection, boolean shouldConvert) {
        for (String key : oldSection.getKeys(false)) {
            //Check if excluded
            boolean shouldConvertDeeper = shouldConvert && !SUtil.contains(key, excludedKeys);

            try {
                String oldKey = key;
                String newKey = key;
                if (shouldConvertDeeper) {
                    newKey = convertKey(oldKey);
                }

                //Check if config
                ConfigurationSection oldOldSection = oldSection.getConfigurationSection(oldKey);
                if (oldOldSection == null) {
                    //Just a value
                    Object oldObject = oldSection.get(oldKey);
                    if (shouldConvertDeeper) {
                        if (oldObject instanceof String) {
                            //Might be a player
                            oldObject = convertKey((String) oldObject);
                        } else if (oldObject instanceof List) {
                            //Check if a String list
                            List plainList = (List) oldObject;
                            if (!plainList.isEmpty()) {
                                if (plainList.get(0) instanceof String) {
                                    List<String> strings = (List<String>) plainList;
                                    //Convert the keys
                                    oldObject = SUtil.toArrayList(strings.stream().map(this::convertKey));
                                }
                            }
                        }
                    }
                    newSection.set(newKey, oldObject);
                } else {
                    //Go a level deeper
                    ConfigurationSection newNewSection = newSection.createSection(newKey);
                    convert(oldOldSection, newNewSection, shouldConvertDeeper);
                }
            } catch (Exception e) {
                Log.warn("Failed, " + oldSection.getName() + " | E: " + e.getMessage());
            }
        }
    }

    private File createFile(String filename) {
        return new File(SlapPlayers.getInstance().getDataFolder() + File.separator + ".." + File.separator + pathToFile + File.separator + filename + ".yml");
    }


    /**
     * Send the CommandSender a message
     * @param message the message
     */
    private void msg(String message) {
        executor.sendMessage(ChatColor.GOLD + "[Converter] " + ChatColor.WHITE + message);
    }

    /**
     * Convert a key into a UUID key
     * @param oldKey the old key
     * @return the new UUID key or the old key if no new key is found
     */
    private String convertKey(String oldKey) {
        //Check if the key is excluded
        if (SUtil.contains(oldKey, excludedKeys)) {
            return oldKey;
        }


        String newKey = oldKey;
        //Find the first name
        Optional<Name> name = uuids.getUserIDs(oldKey).stream()
                .map(uuids::getProfile)
                .map(p ->
                                p.getNames().stream().filter(n -> n.getPlayername().equalsIgnoreCase(oldKey)).findFirst().get()
                )
                .sorted((n1, n2) -> Long.compare(n1.getKnownSince(), n2.getKnownSince()))
                .findFirst();

        //Check if present
        if (name.isPresent()) {
            Profile newProfile = uuids.getProfile(name.get().getProfileID());
            newKey = newProfile.getUUIDString();
            Log.info("[Convert] Converted: " + oldKey + " into UUID of " + newProfile.getCurrentName() + " (" + newProfile.getUUIDString() + ")");
        }

        return newKey;
    }






}
