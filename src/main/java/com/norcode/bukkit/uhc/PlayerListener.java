package com.norcode.bukkit.uhc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerListener implements Listener {
	private UHC plugin;

	public PlayerListener(UHC plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Scoreboard s = plugin.getScoreboard(event.getPlayer());
		if (s != null) {
			event.getPlayer().setScoreboard(s);
		}
	}

	@EventHandler(ignoreCancelled=true, priority = EventPriority.LOW)
	public void onPing(ServerListPingEvent event) {
		event.setMotd("No Game");
		event.setServerIcon(plugin.getPhaseIcon(null));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (plugin.getGame() == null && !event.getPlayer().hasPermission("uhc.staff")) {
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Sorry, the game isn't ready yet.");
		}
	}
}
