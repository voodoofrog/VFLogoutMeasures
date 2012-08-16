package uk.co.forgottendream.vflogoutmeasures;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class LogCrashFilter implements Filter {
	
	private final VFLogoutMeasures plugin;
	
	public LogCrashFilter(VFLogoutMeasures plugin) {
		this.plugin = plugin;
	}
	
	public boolean isLoggable(LogRecord log) {
		String message = log.getMessage().toLowerCase();
		
		if (message.contains("disconnect")) {
			String splitMsg[] = log.getMessage().split(" ");
			String playerName = splitMsg[0];
			
			if (message.contains("genericreason")) {
				plugin.getDataHandler().addToDisputableList(playerName);
				plugin.getVFLogger().warning(playerName + " has logged out *potentially* due to error.");
				return true;
			}
			
			if (message.contains("timeout") || message.contains("overflow") || message.contains("endofstream")) {
				plugin.getDataHandler().addToSafeList(playerName);
				plugin.getVFLogger().warning(playerName + " has logged out due to error and been marked as safe.");
				return true;
			}
			return true;
		}
		return true;
	}
}
