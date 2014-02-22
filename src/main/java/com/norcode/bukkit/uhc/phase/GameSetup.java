package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;

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
}
