package com.norcode.bukkit.uhc;

import com.norcode.bukkit.uhc.command.TeamCommand;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;
import com.wimbli.WorldBorder.WorldFillTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.lang.reflect.Field;
import java.util.HashMap;

public class UHC extends JavaPlugin implements Listener {

	private static final String worldName = "world";
	private int teamIdx = 0;
	private Field reportTotalField;
	private Field reportNumField;
	private Field reportTargetField;
	private Scoreboard teamScoreboard;
	private Scoreboard mainScoreboard;
	private PlayerListener playerListener;
	private int teamSize = 1;
	private int gameLength = 60;
	private GameTimer gameTimer = null;

	private HashMap<String, String> teamCaptains = new HashMap<String, String>();
	private boolean pregame = true;

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
		reflectWB();
		setupScoreboards();
		gameTimer = new GameTimer(this);
		gameTimer.runTaskTimer(this, 20, 20);
		playerListener = new PlayerListener(this);

	}

	private void setupCommands() {
		new TeamCommand(this);
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

	private void reflectWB() {
		try {
			reportTotalField = WorldFillTask.class.getDeclaredField("reportTotal");
			reportNumField = WorldFillTask.class.getDeclaredField("reportNum");
			reportTargetField = WorldFillTask.class.getDeclaredField("reportTarget");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public WorldBorder getWorldBorder() {
		return (WorldBorder) getServer().getPluginManager().getPlugin("WorldBorder");
	}

	public void loadConfig() {
		int sizeX = getConfig().getInt("size.x", 1000);
		int sizeY = getConfig().getInt("size.y", 1000);
		teamSize = getConfig().getInt("team-size", 1);
		World w = getServer().getWorld(worldName);

	}

	public void setupBorder(World world, int x1, int z1, int x2, int z2) {
		Config.setBorderCorners(world.getName(), x1, z1, x2, z2);
	}

	public void startWorldGeneration() {
		Config.fillTask = new WorldFillTask(getServer(), null, worldName, 208, 1, 1, true);
		if (Config.fillTask.valid()) {
			int task = getServer().getScheduler().scheduleSyncRepeatingTask(getWorldBorder(), Config.fillTask, 1, 1);
			Config.fillTask.setTaskID(task);
		}
	}

	public boolean generationComplete() {
		return (Config.fillTask.isPaused() && getGenerationPercentage() == 100);
	}

	public int getGenerationPercentage() {
		int reportTotal;
		int reportNum;
		int reportTarget;

		try {
			reportTotal = (Integer) reportTotalField.get(Config.fillTask);
			reportNum = (Integer) reportNumField.get(Config.fillTask);
			reportTarget = (Integer) reportTargetField.get(Config.fillTask);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return -1;
		}

		double perc = ((double)(reportTotal + reportNum) / (double)reportTarget) * 100;
		if (perc > 100) perc = 100;
		return (int) perc;
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
		return ChatColor.values()[teamIdx++];
	}

	public Scoreboard getTeamScoreboard() {
		return teamScoreboard;
	}

	public Scoreboard getMainScoreboard() {
		return mainScoreboard;
	}

	public boolean isPregame() {
		return pregame == true;
	}

	public Scoreboard getScoreboard(Player player) {
		if (isPregame()) {
			return teamScoreboard;
		} else {
			return mainScoreboard;
		}
	}

	public GameTimer getGameTimer() {
		return gameTimer;
	}

	public int getTeamSize() {
		return teamSize;
	}

	public void checkReady() {
		// TODO: Check if the game is ready to be started (all players are teamed up)
		// TODO: and start the match if its time.
	}
}
