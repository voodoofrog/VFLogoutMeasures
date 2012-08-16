package uk.co.forgottendream.vflogoutmeasures;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;

import sk.tomsik68.slapi.SLAPI;

/**
 * Handles all the data for the plugin
 */
public class DataHandler {

	private final VFLogoutMeasures plugin;
	private Set<String> SafeList = new HashSet<String>();
	private Set<String> NaughtyList = new HashSet<String>();
	private Map<String, String> EmergencyLogouts = new HashMap<String, String>();
	private Map<String, Integer> DisputableLogouts = new HashMap<String, Integer>();
	private Map<String, Long> WorldTimes = new HashMap<String, Long>();
	private String pluginDataFolder;
	
    public DataHandler(VFLogoutMeasures plugin) {
    	this.plugin = plugin;
    	pluginDataFolder = plugin.getDataFolder().getAbsolutePath() + File.separator;
    }
	
	/*public Set<String> getNaughtyList() {
		return NaughtyList;
	}
	
	public void setNaughtyList(HashSet<String> naughtyList) {
		this.NaughtyList = naughtyList;
	}*/
    
	public Map<String, String> getEmergencyLogouts() {
		return EmergencyLogouts;
	}
	
	/*public void setEmergencyLogouts(HashMap<String, String> emergencyLogouts) {
		this.EmergencyLogouts = emergencyLogouts;
	}
	
	public Map<String, Integer> getDisputableLogouts() {
		return DisputableLogouts;
	}
	
	public void setDisputableLogouts(HashMap<String, Integer> disputableLogouts) {
		this.DisputableLogouts = disputableLogouts;
	}*/
	
    /**
     * Adds a player to the naughty list for punishment on login
     *
     * @param player The player to add
     */
	public void addToNaughtyList(Player player) {
		String pn = player.getName();
		NaughtyList.add(pn);
		plugin.getVFLogger().info(pn + " was added to the naughty list.");
	}
	
    /**
     * Removes a player from the naughty list
     *
     * @param player The player to remove
     */
	public void removeFromNaughtyList(Player player) {
		String pn = player.getName();
		NaughtyList.remove(pn);
		plugin.getVFLogger().info(pn + " was removed from the naughty list.");
	}
	
    /**
     * Checks if a player is marked for punishment on login
     *
     * @param player The player to check
     */
	public boolean isPlayerNaughty(Player player) {
		if (NaughtyList.contains(player.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
    /**
     * Adds a player to the safe list
     *
     * @param playername The name of the player to add
     */
	public void addToSafeList(String playername) {
		SafeList.add(playername);
	}
	
    /**
     * Removes a player from the safe list
     *
     * @param player The player to remove
     */
	public void removeFromSafeList(Player player) {
		SafeList.remove(player.getName());
	}
	
    /**
     * Checks if a player is safe from being marked for punishment
     *
     * @param player The player to check
     */
	public boolean isPlayerMarkedSafe(Player player) {
		if (SafeList.contains(player.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
    /**
     * Adds a player to the to the disputable logouts list and increments the disputables
     *
     * @param playername The name of the player to add
     */
	public void addToDisputableList(String playername) {
		if (DisputableLogouts.containsKey(playername)) {
			int numDisputables = DisputableLogouts.get(playername) + 1;
			DisputableLogouts.put(playername, numDisputables);
		} else {
			DisputableLogouts.put(playername, 1);
		}
	}
	
    /**
     * Adds an entry to the emergency logouts list
     *
     * @param playername The name of the player to add
     */
	public void addToEmergencyLogouts(String playername) {
		EmergencyLogouts.put(playername, plugin.getDateTimeFormat().print(plugin.getCurrentDateTime()));
	}
	
    /**
     * Decreases the number of disputable logouts and removes a player if 0
     *
     * @param player The player to remove
     */
	public void removeFromDisputableList(Player player) {
		String pn = player.getName();
		if (DisputableLogouts.containsKey(pn)) {
			int numDisputables = DisputableLogouts.get(pn);
			if (numDisputables > 0) {
				numDisputables = numDisputables - 1;
				DisputableLogouts.put(pn, numDisputables);
			} else {
				DisputableLogouts.remove(player.getName());
			}
		}
	}
	
    /**
     * gets the number of a player's disputable logouts
     *
     * @param player The player to check
     */
	public int getDisputablesNumber(Player player) {
		String pn = player.getName();
		if (DisputableLogouts.containsKey(pn)) {
			int numDisputables = DisputableLogouts.get(pn);
			return numDisputables;
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadData() {
		//load naughty list data from file
		try {
			NaughtyList = (HashSet<String>)SLAPI.load(pluginDataFolder + "VFLMNaughtyList.dat");
			plugin.getVFLogger().info("Naughty list data loaded.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error loading naughty list data: file is missing or not created yet.");
		}
		//load emergency logouts data from file
		try {
			EmergencyLogouts = (HashMap<String, String>)SLAPI.load(pluginDataFolder + "VFLMEmergencyLogouts.dat");
			plugin.getVFLogger().info("Emergency logouts data loaded.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error loading emergency logouts data: file is missing or not created yet.");
		}
		//load disputable logouts data from file
		try {
			DisputableLogouts = (HashMap<String, Integer>)SLAPI.load(pluginDataFolder + "VFLMDisputableLogouts.dat");
			plugin.getVFLogger().info("Disputable logouts data loaded.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error loading disputable logouts data: file is missing or not created yet.");
		}
	}
	
	public void saveData() {
		//save naughty list data to file
		try {
			SLAPI.save(NaughtyList, pluginDataFolder + "VFLMNaughtyList.dat");
			plugin.getVFLogger().info("Naughty list data saved.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error saving naughty list data: " + plugin.getVFLogger().getExceptionAsString(e));
		}
		//save emergency logouts data to file
		try {
			SLAPI.save(EmergencyLogouts, pluginDataFolder + "VFLMEmergencyLogouts.dat");
			plugin.getVFLogger().info("Emergency logout data saved.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error saving emergency logouts data: " + plugin.getVFLogger().getExceptionAsString(e));
		}
		//save disputable logouts data to file
		try {
			SLAPI.save(DisputableLogouts, pluginDataFolder + "VFLMDisputableLogouts.dat");
			plugin.getVFLogger().info("Disputable logouts data saved.");
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error saving disputable logouts data: " + plugin.getVFLogger().getExceptionAsString(e));
		}
	}
	
	public void saveWorldTimesData() {
    	//get the worlds to save ticks for
		List<World> worlds = plugin.getServer().getWorlds();
    	//add the worlds and their ticks to the map
    	for (World world : worlds) {
    		WorldTimes.put(world.getName(), world.getFullTime());
    	}
    	//save the world times data to file
		try {
			SLAPI.save(WorldTimes, pluginDataFolder + "VFLMWorldTimes.dat");
			plugin.getVFLogger().info("World times data saved.");
		} catch (Exception e) {
			plugin.getVFLogger().info("Error saving world times data: " + plugin.getVFLogger().getExceptionAsString(e));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void resetWorldTimes() {
    	//load the world times into the map
		try {
			WorldTimes = (HashMap<String, Long>)SLAPI.load(pluginDataFolder + "VFLMWorldTimes.dat");
			plugin.getVFLogger().info("World times data loaded.");
			//iterate the map and set world times
			for (Map.Entry<String, Long> entry : WorldTimes.entrySet()) {
			    String worldName = entry.getKey();
			    World world = plugin.getServer().getWorld(worldName);
			    world.setFullTime(entry.getValue());
			    plugin.getVFLogger().info("World named " + world.getName() + " has been reset to previous time on file.");
			}
		} catch (Exception e) {
			plugin.getVFLogger().warning("Error loading world times data: world times file missing or not created.");
		}
	}
	
}
