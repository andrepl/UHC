package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scoreboard.Team;

public class MainGame extends Phase {
	private long pvpProtectionDuration = -1;
	public MainGame(UHC plugin) {
		super(plugin, "Main-Game");
		this.duration = plugin.getConfig().getInt("main-phase-seconds", 60*60) * 1000;
		this.pvpProtectionDuration = plugin.getConfig().getInt("pvp-protection-seconds", 30) * 1000;
	}

	@Override
	public void onStart() {
		plugin.getLogger().info("MainGame starting...");
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("UHC Has Begun!");
			p.setScoreboard(plugin.getMainScoreboard());

		}
		World world = plugin.getUHCWorld();
		world.setDifficulty(Difficulty.HARD);
		world.setGameRuleValue("doDaylightCycle", "true");
		world.setTime(800);
		world.setPVP(false);

		for (World w: plugin.getServer().getWorlds()) {
			w.setGameRuleValue("naturalRegeneration", "false");
		}
		for (Team t: plugin.getMainScoreboard().getTeams()) {
			t.setAllowFriendlyFire(false);
		}
		if (pvpProtectionDuration > -1) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					World world = plugin.getUHCWorld();
					world.setPVP(true);
				}
			}, 20*(pvpProtectionDuration/1000));
		} else {
			world.setPVP(true);
		}
		plugin.getLogger().info("MainGame started...");
	}

	@Override
	public String formatMessage(Game game) {
		if (getElapsedTime() <= pvpProtectionDuration) {
			return "PVP will be enabled in " + formatSecondsRemaining((int) ((pvpProtectionDuration - getElapsedTime())/1000));
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
		if (plugin.isParticipant(event.getPlayer().getName())) {
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