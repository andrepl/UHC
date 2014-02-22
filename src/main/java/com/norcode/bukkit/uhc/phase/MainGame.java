package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.entity.Player;

public class MainGame extends Phase {
	public MainGame(UHC plugin) {
		super(plugin, "Main-Game");
		this.duration = 1000 * 60;
	}

	@Override
	public void onStart() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("UHC Has Begun!");
		}
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Main-Game has Ended.");
		}
	}
}
