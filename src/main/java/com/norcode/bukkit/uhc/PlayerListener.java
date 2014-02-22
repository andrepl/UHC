package com.norcode.bukkit.uhc;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	private UHC plugin;

	public PlayerListener(UHC plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.getGameTimer().registerPlayer(event.getPlayer());
		event.getPlayer().setScoreboard(plugin.getScoreboard(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.getGameTimer().unregisterPlayer(event.getPlayer());
	}
}
