package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import com.norcode.bukkit.uhc.UHCError;
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
			p.sendMessage("Pre game has started.  You must join a team before the timer expires.  Type /team <name> to create your own team");
		}
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
		event.getPlayer().sendMessage("Welcome to Mine vs Mine UHC!  Type /team to get started or ask in chat if you need a team");
		event.getPlayer().teleport(plugin.getUHCWorld().getSpawnLocation());
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			p.sendMessage("Pre-game is over.");
			if (p.hasPermission("uhc.staff") || plugin.isParticipant(p.getName())) {
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