package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import com.norcode.bukkit.uhc.UHCError;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scoreboard.Team;

public class PreGame extends Phase {
	int teamSize;

	public boolean isFull() {
		return plugin.getMainScoreboard().getTeams().size() >= 15;
	}

	public PreGame(UHC plugin) {
		super(plugin, "Pre-Game");
		this.duration = plugin.getConfig().getInt("pregame-phase-seconds", 10*60) * 1000;
		teamSize = plugin.getConfig().getInt("team-size", 1);
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
		World world = plugin.getServer().getWorld("world");
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(800);
		world.setPVP(false);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(plugin.getTeamScoreboard());
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			Team team = plugin.getMainScoreboard().getPlayerTeam(event.getPlayer());
			if (team == null) {
				if (teamSize == 1) {
					try {
						plugin.registerTeam(event.getPlayer().getName(), event.getPlayer());
					} catch (UHCError error) {
						error.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Pre-game is over.");
			if (p.hasPermission("uhc.staff") || plugin.isPlayerAllowed(p)) {
				continue;
			}
			p.kickPlayer("The game has started without you");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (isFull()) {
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Sorry, the game is full.");
		} else if (plugin.getBanList().isPlayerBanned(event.getPlayer())) {
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
					"You're Banned until: " + plugin.getBanList().getBan(event.getPlayer()).getExpiryString());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("Pre-Game In Progress");
		event.setServerIcon(plugin.getPhaseIcon(PreGame.class));
	}
}