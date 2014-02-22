package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PreGame extends Phase {

	public PreGame(UHC plugin) {
		super(plugin, "Pre-Game");
		this.duration = 1000 * 30;
	}

	@Override
	public String formatMessage(Game game) {
		int secondsElapsed = (int) (getElapsedTime() / 1000);
		int durationSeconds = (int) (getDuration()/ 1000);
		int secondsRemaining = durationSeconds - secondsElapsed;
		return "UHC Begins in " + formatSecondsRemaining(secondsRemaining);
	}

	@Override
	public void onStart() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.setScoreboard(plugin.getTeamScoreboard());
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(plugin.getTeamScoreboard());
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Pre-game is over.");
		}
	}
}
