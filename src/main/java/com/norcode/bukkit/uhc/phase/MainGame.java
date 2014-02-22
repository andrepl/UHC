package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.UHC;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

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
}
