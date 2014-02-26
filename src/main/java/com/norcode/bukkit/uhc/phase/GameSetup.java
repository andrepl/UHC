package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class GameSetup extends Phase {

	private boolean regen;

	public GameSetup(UHC plugin, boolean regen) {
		super(plugin, "Game-Setup");
		this.regen = regen;
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
		plugin.getWorldSetup().start(regen);
	}

	@Override
	public void onEnd() {
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("Game Setup In Progress");
		event.setServerIcon(plugin.getPhaseIcon(GameSetup.class));
	}

}
