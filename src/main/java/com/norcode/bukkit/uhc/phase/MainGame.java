package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scoreboard.Team;

public class MainGame extends Phase {
	private long friendFireDisabledTime = -1;
	public MainGame(UHC plugin) {
		super(plugin, "Main-Game");
		this.duration = plugin.getConfig().getInt("main-phase-seconds", 60*60) * 1000;
		this.friendFireDisabledTime = plugin.getConfig().getInt("friendly-fire-protection-seconds", 30) * 1000;
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
		for (Team t: plugin.getMainScoreboard().getTeams()) {
			t.setAllowFriendlyFire(false);
		}
		if (friendFireDisabledTime > -1) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					setAllowFriendlyFire();
				}
			}, 20*(friendFireDisabledTime/1000));
		}
	}

	public void setAllowFriendlyFire() {
		for (Team t: plugin.getMainScoreboard().getTeams()) {
			t.setAllowFriendlyFire(true);
		}
	}

	@Override
	public String formatMessage(Game game) {
		if (getElapsedTime() <= friendFireDisabledTime) {
			return "Friendly fire will be enabled in " + formatSecondsRemaining((int) ((friendFireDisabledTime - getElapsedTime())/1000));
		}
		return message;
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

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer().hasPermission("uhc.staff")) {
			return;
		}
		if (plugin.isPlayerAllowed(event.getPlayer())) {
			return;
		}
		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You can't join a game in progress unless you were registered in pre-game.");
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("UHC In Progress");
		event.setServerIcon(plugin.getPhaseIcon(MainGame.class));
	}
}
