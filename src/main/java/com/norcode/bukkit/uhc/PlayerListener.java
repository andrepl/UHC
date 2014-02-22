package com.norcode.bukkit.uhc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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

}
