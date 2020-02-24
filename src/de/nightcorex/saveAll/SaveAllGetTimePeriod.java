package de.nightcorex.saveAll;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class SaveAllGetTimePeriod implements CommandExecutor {

    private final Main plugin;

    //Milliseconds -> second (1L * 1000L)
    private static final long SECOND = 1000L;

    //Milliseconds -> seconds -> minute (SECOND * 60L)
    private static final long MINUTE = 60_000L;

    private final DecimalFormat df = new DecimalFormat("#.#");

    public SaveAllGetTimePeriod(Main plugin) {
        df.setRoundingMode(RoundingMode.HALF_UP);
        this.plugin = plugin;
    }

    /**
     * This method sends a message to the respective {@link CommandSender}.
     * Note that this message is in the following format: M+:SS.L
     * <p>
     * Since version 1.1 this command also prints the remaining time to the next save-all execution
     *
     * @param sender The recipient of the message
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            p.sendMessage("§bAutosave Refresh Rate (M+:S+): " + Main.getAutosaveTimeMinutes() + ":" +
                    String.format("%02d",Main.getAutosaveTimeSeconds()));
            p.sendMessage("§bNext refresh in " + calculateRemainingTime());
        } else {
            System.out.println("Autosave Refresh Rate (M+:S+): " + Main.getAutosaveTimeMinutes() + ":" +
                    String.format("%02d",Main.getAutosaveTimeSeconds()));
            System.out.println("Next refresh in " + calculateRemainingTime());
        }
        return true;
    }

    public String calculateRemainingTime() {
        long passedTime = System.currentTimeMillis() - plugin.lastUpdate;
        long remainingTime = (Main.getAutosaveTimeMinutes() * MINUTE + Main.getAutosaveTimeSeconds() * SECOND) - passedTime;

        //Minutes
        int minutes = (int) (remainingTime / MINUTE);
        remainingTime -= minutes * MINUTE;

        //Seconds
        double seconds = remainingTime / (double) SECOND;

        //There is a better way to do this 100% but I just don't know about it. So this is the temporary solution
        return minutes + ":" + (seconds >= 10 ? df.format(seconds) : "0" + df.format(seconds));
    }
}
