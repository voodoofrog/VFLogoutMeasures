package uk.co.forgottendream.vflogoutmeasures;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import uk.co.forgottendream.vflogger.VFLogger;

/**
 * Main plugin class
 */
public class VFLogoutMeasures extends JavaPlugin {

	private static VFLogoutMeasures plugin;
	private VFLMDataHandler dataHandler;
	private VFLMEventsListener listeners;
	private VFLogger vflogger;
	private DateTime currentDateTime;
		
	public void onEnable() {
		//setup instances
		plugin = this;
		dataHandler = new VFLMDataHandler(this);
		listeners = new VFLMEventsListener(this);
		vflogger = new VFLogger(this.getName());
		currentDateTime = new DateTime();
		//setup data path
		//add a log filter for crashes
		getLogger().setFilter(new VFLMLogCrashFilter(this));
		//read config and set defaults
		getConfig().options().copyDefaults(true);
		//set data from config
		//save config
		saveConfig();
		//load the plugin data from files
		dataHandler.loadData();
		//register the listener
		getServer().getPluginManager().registerEvents(listeners, this);
		getLogger().info("VFLogoutMeasures plugin has been enabled.");
	}
	 
	public void onDisable() {
		//save the plugin data to files
		dataHandler.saveData();
		//nullify instances
		currentDateTime = null;
		vflogger = null;
		dataHandler = null;
		plugin = null;
		getLogger().info("VFLogoutMeasures plugin has been disabled.");
	}
	
    /**
     * Gets the {@link VFLMDataHandler} instance attached to the plugin instance
     *
     * @return VFLMDataHandler instance
     */
	public VFLMDataHandler getDataHandler()
	{
		return dataHandler;
	}
	
    /**
     * Gets the running instance of the plugin
     *
     * @return plugin instance
     */
	public static VFLogoutMeasures getPlugin()
	{
		return plugin;
	}
	
    /**
     * Gets the {@link VFLogger} instance attached to the plugin instance
     *
     * @return VFLogger instance
     */
	public VFLogger getVFLogger() {
		return vflogger;
	}
	
    /**
     * Gets the current joda datetime
     *
     * @return currentDateTime object
     */
	public DateTime getCurrentDateTime() {
		return currentDateTime;
	}
	
	public DateTimeFormatter getDateTimeFormat() {
		DateTimeFormatter formattedDT = ISODateTimeFormat.dateTime();
		return formattedDT;
	}

	//perhaps add a command to wipe all data from the console when server is empty
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if(cmd.getName().equalsIgnoreCase("emergencylogout")) {
			if (player == null) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				//check if player has previously used an emergency logout
				String pn = player.getName();
				if (dataHandler.getEmergencyLogouts().containsKey(pn)) {
					//player has used this before so check the last emergency logout time
					DateTime lastTime = getDateTimeFormat().parseDateTime(dataHandler.getEmergencyLogouts().get(pn));
					//check if 24 hours have passed
					double hoursPassed = (getCurrentDateTime().getMillis() - lastTime.getMillis()) / 1000 / 60 / 60;
					if (hoursPassed >= 24) {
						emergencyLogout(player);
					} else {
						sender.sendMessage("It has not been 24 hours since your last emergency logout.");
					}
				} else {
					//player has never emergency logged out before so just do it
					emergencyLogout(player);
				}
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("toggletimesaving")) {
			if (getConfig().getBoolean("SaveTimeOnEmpty")) {
				getConfig().set("SaveTimeOnEmpty", false);
				saveConfig();
				sender.sendMessage("World time saving on last logout disabled.");
			} else {
				getConfig().set("SaveTimeOnEmpty", true);
				saveConfig();
				sender.sendMessage("World time saving on last logout enabled.");
			}

			return true;
		}
		return false; 
	}
	
    /**
     * kicks the player from the game and logs it
     *
     * @param player The player to kick
     */
	public void emergencyLogout(Player player) {
		String pn = player.getName();
		//prevent the player from being marked for punishment
		dataHandler.addToSafeList(player.getName());
		//kick the player and log it
		player.kickPlayer("Emergency Logout: you must wait 24 hours before using again.");
		plugin.getVFLogger().info(player.getName() + " has used an emergency logout.");
		//record the date & time in the emergency log list
		dataHandler.addToEmergencyLogouts(pn);
	}
}
