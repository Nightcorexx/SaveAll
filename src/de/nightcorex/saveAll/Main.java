package de.nightcorex.saveAll;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Runs the "/save-all" command every {@link #AUTOSAVE_TIME_MINUTES} minutes and {@link #AUTOSAVE_TIME_SECONDS} seconds.
 * <p>
 * Another feature to be added is the option to disable the output in the chat/only display it to users with the
 * specific permission
 * <p>
 * There may be a bug where, if you edit the autosave time in the exact moment the thread is not in sleep state,
 * this plugin may crash. If so then please contact a developer.
 */
public class Main extends JavaPlugin {


    /**
     * Defines the time period after which the "/save-all" command will be executed (in minutes)
     *
     * @implNote since 1.1 value gets overwritten on initialization if a time exists in the {@link #timeConfig}
     */
    private static int AUTOSAVE_TIME_MINUTES = 10;

    /**
     * Defines the time period after which the "/save-all" command will be executed (in seconds)
     *
     * @implNote since 1.1 value gets overwritten on initialization if a time exists in the {@link #timeConfig}
     */
    private static int AUTOSAVE_TIME_SECONDS = 0;

    /**
     * The thread which will be used to execute the "/save-all" command (so it won't sleep the main thread)
     */
    private Thread saveAllThread = new Thread(this::saveAll);

    /**
     * Signals the thread if it should continue running or be ended (in this case the onEnable method)
     */
    private static volatile boolean stopThread = false;

    /**
     * File of the time config (aka. saving the values, so that they can be stored even though server may restart)
     *
     * @see #timeConfig
     * @since 1.1
     */
    private File timeConfigFile;

    /**
     * FileConfiguration of the time config (aka. saving the values, so that they can be
     * stored even though server may restart)
     *
     * @see #timeConfig
     * @since 1.1
     */
    private FileConfiguration timeConfig;

    /**
     * Saves the time of the last save-all execution using System.currentTimeMillis()
     *
     * @since 1.1
     */
    long lastUpdate;

    @Override
    public void onEnable() {
        createTimeConfig();
        readTimeVariables();
        registerCommands();
        saveAllThread.start();
        lastUpdate = System.currentTimeMillis();
    }

    /**
     * initialises the {@link #timeConfig} and the {@link #timeConfigFile}. Creates latter one if it doesn't exist, yet.
     *
     * @since 1.1
     */
    private void createTimeConfig() {
        timeConfigFile = new File(getDataFolder(), "time.yml");
        if(!timeConfigFile.exists()) {
            timeConfigFile.getParentFile().mkdirs();
        }
        timeConfig = new YamlConfiguration();
        try {
            timeConfigFile.createNewFile();
            timeConfig.load(timeConfigFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialises {@link #AUTOSAVE_TIME_MINUTES} and {@link #AUTOSAVE_TIME_SECONDS}
     * if a time is present in {@link #timeConfig}. Otherwise creates this entry with a standard time period of
     * "{@link #AUTOSAVE_TIME_MINUTES}:{@link #AUTOSAVE_TIME_SECONDS}"
     *
     * @since 1.1
     */
    private void readTimeVariables() {
        String period = readFromTimeConfig();
        if(period != null) {
            String[] periodSplit = period.split(":");
            try {
                int minutes = Integer.parseInt(periodSplit[0]);
                int seconds = Integer.parseInt(periodSplit[1]);
                setAutosaveTimeMinutes(minutes);
                setAutosaveTimeSeconds(seconds);
                return;
            } catch (NumberFormatException e) {
                System.err.println("Could not parse the refresh-rate. Overwriting existing values with standard values now");
            }
        }
        writeToTimeConfig(getAutosaveTimeMinutes() + ":" + getAutosaveTimeSeconds());
    }

    private void registerCommands() {
        this.getCommand("sa").setExecutor(new SaveAllSetTimePeriod(this));
        this.getCommand("sa-get").setExecutor(new SaveAllGetTimePeriod(this));
    }

    @Override
    public void onDisable() {
        stopThread();
    }

    private void saveAll() {
        for (; ; ) {
            try {
                Thread.sleep(AUTOSAVE_TIME_MINUTES * 60 * 1000 + AUTOSAVE_TIME_SECONDS * 1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
                if(stopThread) {
                    break;
                }
            }
            dispatchSaveAllCommand();
            lastUpdate = System.currentTimeMillis();
        }
    }

    private static void dispatchSaveAllCommand() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
    }

    public FileConfiguration getTimeConfig() {
        return timeConfig;
    }

    /**
     * writes a given value to the {@link #timeConfig}
     *
     * @param data written to the {@link #timeConfig}
     * @since 1.1
     */
    public void writeToTimeConfig(String data) {
        getTimeConfig().set("refresh-rate", data);
        try {
            getTimeConfig().save(timeConfigFile);
        } catch (IOException e) {
            System.err.println("Overwriting the values for this session but please note that it won't be permanent " +
                    "as an error occurred while saving the timeConfig");
        }

    }

    /**
     * @return the "refresh-rate" saved in the {@link #timeConfig}
     * @since 1.1
     */
    public String readFromTimeConfig() {
        return getTimeConfig().getString("refresh-rate");
    }

    public static int getAutosaveTimeMinutes() {
        return AUTOSAVE_TIME_MINUTES;
    }

    public static void setAutosaveTimeMinutes(int autosaveTimeMinutes) {
        AUTOSAVE_TIME_MINUTES = autosaveTimeMinutes;
    }

    public static int getAutosaveTimeSeconds() {
        return AUTOSAVE_TIME_SECONDS;
    }

    public static void setAutosaveTimeSeconds(int autosaveTimeSeconds) {
        AUTOSAVE_TIME_SECONDS = autosaveTimeSeconds;
    }

    public void restartThread() {
        saveAllThread.interrupt();
    }

    private void stopThread() {
        stopThread = true;
        saveAllThread.interrupt();
        dispatchSaveAllCommand();
    }

}
