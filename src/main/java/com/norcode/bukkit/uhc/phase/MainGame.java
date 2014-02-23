package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class MainGame extends Phase {
	public MainGame(UHC plugin) {
		super(plugin, "Main-Game");
		this.duration = 1000 * 60;
	}

	@Override
	public void onStart() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("UHC Has Begun!");
			p.setScoreboard(plugin.getMainScoreboard());
		}
		for (World w: plugin.getServer().getWorlds()) {
			w.setGameRuleValue("naturalRegeneration", "false");
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(plugin.getMainScoreboard());
	}
	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Main-Game has Ended.");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer().hasPermission("uhc.staff")) {
			return;
		}
		if (plugin.isPlayerAllowed(event.getPlayer())) {
			return;
		}
		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You can't join a game in progress unless you were registered in pre-game.");
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		//need to update status here of the match.
	}
}
