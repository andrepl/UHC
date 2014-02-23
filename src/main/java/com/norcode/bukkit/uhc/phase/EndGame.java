package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class EndGame extends Phase {

	public EndGame(UHC plugin) {
		super(plugin, "End-Game");
		this.duration = plugin.getConfig().getInt("endgame-phase-seconds", 10*60) * 1000;
	}

	@Override
	public void onStart() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("It's now Endgame.");
		}
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Endgame is over.");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer().hasPermission("uhc.staff")) {
			return;
		}
		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You can't join a game currently in end game");
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("End Game In Progress");
	}
}
