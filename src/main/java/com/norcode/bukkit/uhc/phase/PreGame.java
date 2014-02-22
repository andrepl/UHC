package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.entity.Player;

public class PreGame extends Phase {

	public PreGame(UHC plugin) {
		super(plugin, "Pre-Game");
		this.duration = 1000 * 30;
	}

	@Override
	public String formatMessage(Game game) {
		int secondsElapsed = (int) (game.getElapsedTime() / 1000);
		int durationSeconds = (int) (getDuration()/ 1000);
		int secondsRemaining = durationSeconds - secondsElapsed;
		return "UHC Begins in " + formatSecondsRemaining(secondsRemaining);
	}

	@Override
	public void onStart() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Pre-game is now.");
		}
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Pre-game is over.");
		}
	}
}
