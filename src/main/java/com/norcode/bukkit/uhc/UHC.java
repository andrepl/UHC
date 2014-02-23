package com.norcode.bukkit.uhc;

import com.norcode.bukkit.uhc.command.TeamCommand;
import com.norcode.bukkit.uhc.command.UHCCommand;
import com.norcode.bukkit.uhc.phase.EndGame;
import com.norcode.bukkit.uhc.phase.GameSetup;
import com.norcode.bukkit.uhc.phase.MainGame;
import com.norcode.bukkit.uhc.phase.PreGame;
import com.norcode.bukkit.uhc.phase.Scatter;
import com.wimbli.WorldBorder.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class UHC extends JavaPlugin implements Listener {


	private int teamIdx = 0;

	private List<ChatColor> colors = new ArrayList<ChatColor>();
	private Scoreboard teamScoreboard;
	private Scoreboard mainScoreboard;
	private PlayerListener playerListener;
	private int gameLength = 60;
	private Game game = null;
	private HashMap<String, String> teamCaptains = new HashMap<String, String>();
	private HashMap<String, Location> teamSpawnLocations = new HashMap<String, Location>();
	private WorldSetup worldSetup;

	public OfflinePlayer getTeamCaptain(String team) {
		String name = teamCaptains.get(team);
		if (name != null) {
			return Bukkit.getOfflinePlayer(name);
		}
		return null;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadConfig();
		setupCommands();
		setupScoreboards();
		playerListener = new PlayerListener(this);
		for (ChatColor c: ChatColor.values()) {
			if (c == ChatColor.RESET
					|| c == ChatColor.BOLD
					|| c == ChatColor.MAGIC
					|| c == ChatColor.UNDERLINE
					|| c == ChatColor.ITALIC
					|| c == ChatColor.STRIKETHROUGH) {
				continue;
			}
			colors.add(c);
		}
	}

	public void findSpawnLocations() {

	}

	private void setupCommands() {
		new TeamCommand(this);
		new UHCCommand(this);
	}

	private void setupScoreboards() {
		teamScoreboard = getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = teamScoreboard.registerNewObjective("members", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName("Teams");

		mainScoreboard = getServer().getScoreboardManager().getNewScoreboard();
		obj = mainScoreboard.registerNewObjective("playerHealth", "Health");
		obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		obj.setDisplayName("HP");
	}


	public WorldBorder getWorldBorder() {
		return (WorldBorder) getServer().getPluginManager().getPlugin("WorldBorder");
	}

	public void loadConfig() {
		//teamSize = getConfig().getInt("team-size", 1);
		String worldName = getConfig().getString("world-name");
		worldSetup = new WorldSetup(this, worldName);
	}


	public Team registerTeam(String name, Player captain) throws UHCError {
		String slug = slugify(name);
		Team team = mainScoreboard.getTeam(slug);
		if (team != null) {
			throw new UHCError("Team " + slug + " already exists.");
		}
		if (mainScoreboard.getTeams().size() == 16) {
			throw new UHCError("The maximum number of teams has been reached.");
		}
		team = mainScoreboard.registerNewTeam(slug);
		team.setDisplayName(name);
		team.setPrefix(getNextTeamColor().toString());
		team.setSuffix(ChatColor.RESET.toString());
		team.addPlayer(captain);
		teamCaptains.put(slug, captain.getName());


		Score s = teamScoreboard.getObjective("members").getScore(Bukkit.getOfflinePlayer(slug));
		s.setScore(1);
		return team;
	}

	private void addToTeam(Player player, Team team) {
		team.addPlayer(player);
		Score s = teamScoreboard.getObjective("members").getScore(Bukkit.getOfflinePlayer(team.getName()));
		s.setScore(s.getScore() + 1);
	}

	private String slugify(String s) {
		s = s.toLowerCase().replace("[^A-Za-z0-9]", "_");
		return s.substring(0, Math.min(s.length(), 16));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(getTeamScoreboard());
	}

	public ChatColor getNextTeamColor() {
		return colors.get(teamIdx++);
	}

	public Scoreboard getTeamScoreboard() {
		return teamScoreboard;
	}

	public Scoreboard getMainScoreboard() {
		return mainScoreboard;
	}

	public Scoreboard getScoreboard(Player player) {
		if (game == null) {
			return null;
		}
		if (game.getCurrentPhase() instanceof PreGame) {
			return teamScoreboard;
		} else {
			return mainScoreboard;
		}
	}

	public Game getGame() {
		return game;
	}

	public int getTeamSize() {
		return getConfig().getInt("team-size");
	}

	public void checkReady() {
		// TODO: Check if the game is ready to be started (all players are teamed up)
		// TODO: and start the match if its time.
	}

	public void startGame() {
		game = new Game(this);
		game.addPhase(new PreGame(this));
		game.addPhase(new Scatter(this));
		game.addPhase(new MainGame(this));
		game.addPhase(new EndGame(this));
		game.start();
	}

	public void startSetup() {
		game = new Game(this);
		game.addPhase(new GameSetup(this));
		game.start();
	}

	public WorldSetup getWorldSetup() {
		return worldSetup;
	}

	public void gameOver() {

		this.game = null;
	}

	public World getUHCWorld() {
		return getServer().getWorld(getConfig().getString("world-name"));
	}

	public boolean isPlayerAllowed(Player p){
		Set<Team> teams = this.getMainScoreboard().getTeams();
		Set<OfflinePlayer> allowedPlayers = new HashSet<OfflinePlayer>();
		for (Team team: teams) {
			for (OfflinePlayer player: team.getPlayers()) {
				allowedPlayers.add(player);
			}
		}
		OfflinePlayer op = (OfflinePlayer) p;
		if(allowedPlayers.contains(op)) {
			return true;
		}
		return false;
	}

}
