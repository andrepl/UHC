package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class GameSetup extends Phase {

	public GameSetup(UHC plugin) {
		super(plugin, "Game-Setup");
	}

	@Override
	public String formatMessage(Game game) {
		return "Map setup is in progress.";
	};

	@Override
	public boolean isOver() {
		boolean over = plugin.getWorldSetup().isComplete();
		return over;
	}

	@Override
	public int getPercentage() {
		return plugin.getWorldSetup().getPercentage();
	}

	@Override
	public void onStart() {
		plugin.getWorldSetup().start();
	}

	@Override
	public void onEnd() {
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("Game Setup In Progress");
		event.setServerIcon(plugin.getPhaseIcon(GameSetup.class));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		event.getPlayer().sendMessage("You have joined a game during initial setup");
		event.getPlayer().sendMessage("This can take a while to generate the world to be played");
	}
}
