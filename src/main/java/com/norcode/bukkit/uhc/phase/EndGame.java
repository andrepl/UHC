package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.entity.Player;

public class EndGame extends Phase {

	public EndGame(UHC plugin) {
		super(plugin, "End-Game");
		this.duration = 1000* 30;
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
}
