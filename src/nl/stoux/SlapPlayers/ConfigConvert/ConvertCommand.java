package nl.stoux.SlapPlayers.ConfigConvert;

import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Stoux on 06/02/2015.
 */
public class ConvertCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }

        String[] excludedKeys = new String[args.length - 2];
        for (int i = 2; i < args.length; i++) {
            excludedKeys[i - 2] = args[i];
        }

        //Do this in async
        Bukkit.getScheduler().runTaskAsynchronously(SlapPlayers.getInstance(), () -> {
            Converter converter = new Converter(commandSender, args[0], args[1], excludedKeys);
            converter.convert();
        });
        return true;
    }
}
