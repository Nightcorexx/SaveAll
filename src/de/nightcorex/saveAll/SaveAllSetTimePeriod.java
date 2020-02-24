package de.nightcorex.saveAll;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveAllSetTimePeriod implements CommandExecutor {

    private final Main plugin;

    public SaveAllSetTimePeriod(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(args.length != 1) {
            errorHandling(sender);
            return false;
        }

        String[] time = args[0].split(":");

        if(time.length == 1) {
            //Only seconds
            try {
                int seconds = Integer.parseInt(time[0]);

                plugin.writeToTimeConfig(convertTime(0, seconds));

                successHandling(sender);
                plugin.restartThread();
                return true;
            } catch (NumberFormatException e) {
                errorHandling(sender);
                return false;
            }
        } else if(time.length == 2) {
            //Minutes:Seconds
            try {
                int minutes = Integer.parseInt(time[0]);
                int seconds = Integer.parseInt(time[1]);

                plugin.writeToTimeConfig(convertTime(minutes, seconds));

                successHandling(sender);
                plugin.restartThread();
                return true;
            } catch (NumberFormatException e) {
                errorHandling(sender);
                return false;
            }
        } else {
            errorHandling(sender);
            return false;
        }

    }

    private String convertTime(int minutes, int seconds) {
        if(seconds / 60 > 0) {
            minutes += (seconds / 60);
            seconds %= 60;
        }
        Main.setAutosaveTimeMinutes(minutes);
        Main.setAutosaveTimeSeconds(seconds);

        return minutes + ":" + seconds;
    }

    private void successHandling(CommandSender sender) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            p.sendMessage("§aSave-Periode erfolgreich umgestellt");
        } else {
            System.out.print("Save-Periode erfolgreich umgestellt");
        }
    }

    private void errorHandling(CommandSender sender) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            p.sendMessage("§4Usage: /sa M+:S+ | /sa S+");
        } else {
            System.out.print("Usage: /sa M+:S+ | /sa S+");
        }
    }
}
