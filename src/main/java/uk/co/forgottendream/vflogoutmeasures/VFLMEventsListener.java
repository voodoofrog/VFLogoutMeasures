package uk.co.forgottendream.vflogoutmeasures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class VFLMEventsListener implements Listener {

	private final VFLogoutMeasures plugin;
	private Map<String, Integer> scheduledLogouts = new HashMap<String, Integer>();
	
	public VFLMEventsListener(VFLogoutMeasures plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		Server server = player.getServer();
		//if first to log in set ticks
	    if (server.getOnlinePlayers().length == 0 && plugin.getConfig().getBoolean("SaveTimeOnEmpty")) {
	    	plugin.getVFLogger().info(player.getName() + " is the first player to log in.");
	    	plugin.getDataHandler().resetWorldTimes();
	    }
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
	    //punish if player last quit improperly
		if (plugin.getDataHandler().isPlayerNaughty(player)) {
			player.setHealth(0);
			player.sendMessage("You have been killed for logging out in the wilderness.");
			plugin.getVFLogger().info(player.getName() + " has been punished with death.");
			//remove from list
			plugin.getDataHandler().removeFromNaughtyList(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Server server = player.getServer();
		
		if (!plugin.getDataHandler().isPlayerMarkedSafe(player)) { //if not excluded from punishment
			//mark the player for punishment
			plugin.getDataHandler().addToNaughtyList(player);
			plugin.getVFLogger().info(player.getName() + " has logged out unsafely and been marked for punishment.");
		}
		
		//remove player from safe list
		plugin.getDataHandler().removeFromSafeList(player);
		
		//check if is the last player and save ticks if enabled
	    if (server.getOnlinePlayers().length - 1 <= 0 && plugin.getConfig().getBoolean("SaveTimeOnEmpty")) {
	    	plugin.getVFLogger().info(player.getName() + " is the last player to log out.");
	    	plugin.getDataHandler().saveWorldTimesData();
	    }
	}
	
	//cancel normal bed use
	@EventHandler
	public void onPlayerBed(final PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	//detects players interacting with beds
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (event.getClickedBlock().getType().equals(Material.BED_BLOCK))) {
			Player player = event.getPlayer();
			Location bedLoc = event.getClickedBlock().getLocation();
			//set players spawn to this bed
			player.setBedSpawnLocation(bedLoc);
			player.sendMessage("This bed has been set as your spawn location.");
			//check for monsters nearby and cancel logout if true
			List<Entity> nearbyEntities = player.getNearbyEntities(10.0D, 10.0D, 10.0D);
			if (!nearbyEntities.isEmpty()) {
				for(Entity ent : nearbyEntities)
				{
				    if(ent instanceof Monster)
				    {
				    	player.sendMessage("It is not safe to logout there are monsters nearby.");
				    	return;
				    }
				}
			}
			//add a scheduled logout for the player
			player.sendMessage("You will be logged out in 3 seconds.");
			player.sendMessage("Move to cancel.");
			int taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Player player = event.getPlayer();
					plugin.getDataHandler().addToSafeList(player.getName());
					//remove from scheduled logouts map
					scheduledLogouts.remove(player.getName());
					player.kickPlayer("You have safely logged out.");
				}
			}, 60L);
			//add player to the map of scheduled logouts
			scheduledLogouts.put(player.getName(), taskID);
			//stops silly "you can only sleep at night messages"
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String pn = player.getName();
		//only if player is moving location (not mouse)
		if (event.getFrom().getY() != event.getTo().getY() ||
		event.getFrom().getX() != event.getTo().getX() || 
		event.getFrom().getZ() != event.getTo().getZ()) {
			//only if the scheduled logouts map has entries and one of them is the player
			if (!scheduledLogouts.isEmpty()) {
				if (scheduledLogouts.containsKey(pn)) {
					//cancel the scheduled logout
					int taskID = scheduledLogouts.get(pn);
					plugin.getServer().getScheduler().cancelTask(taskID);
					player.sendMessage("Logout cancelled.");
					scheduledLogouts.remove(pn);
				}
			}
		}
	}
	
	@EventHandler
	public void onWorldSave(WorldSaveEvent event) {
		plugin.getVFLogger().info("World save triggering data save:");
		plugin.getDataHandler().saveData();
	}
	
	@EventHandler
	public void onSaveAll(ServerCommandEvent event) {
		String command = event.getCommand();
		if (command.equalsIgnoreCase("save-all")) {
			plugin.getVFLogger().info("save-all triggering data save:");
			plugin.getDataHandler().saveData();
		}
	}

}
