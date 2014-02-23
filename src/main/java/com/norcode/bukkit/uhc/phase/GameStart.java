package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class GameStart extends Phase {
	public GameStart(UHC plugin) {
		super(plugin, "Game-Start");
	}

	@Override
	public void onStart() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onEnd() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("Game has started");
	}
}
