package com.norcode.bukkit.uhc;

import com.norcode.bukkit.uhc.command.ScratchCommand;
import com.norcode.bukkit.uhc.command.TeamCommand;
import com.norcode.bukkit.uhc.command.UHCCommand;
import com.norcode.bukkit.uhc.phase.EndGame;
import com.norcode.bukkit.uhc.phase.GameSetup;
import com.norcode.bukkit.uhc.phase.MainGame;
import com.norcode.bukkit.uhc.phase.Phase;
import com.norcode.bukkit.uhc.phase.PreGame;
import com.norcode.bukkit.uhc.phase.Scatter;
import com.wimbli.WorldBorder.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
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
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UHC extends JavaPlugin implements Listener {


	private int teamIdx = 0;

	private List<ChatColor> colors = new ArrayList<ChatColor>();
	private HashMap<Class<? extends Phase>, CachedServerIcon> phaseIcons = new HashMap<Class<? extends Phase>, CachedServerIcon>();
	private Scoreboard teamScoreboard;
	private Scoreboard mainScoreboard;
	private PlayerListener playerListener;
	private int gameLength = 60;
	private Game game = null;
	private HashMap<String, String> teamCaptains = new HashMap<String, String>();
	private HashMap<String, Location> teamSpawnLocations = new HashMap<String, Location>();
	private WorldSetup worldSetup;
	private UHCBanList banList;

	public OfflinePlayer getTeamCaptain(String team) {
		String name = teamCaptains.get(team);
		if (name != null) {
			return Bukkit.getOfflinePlayer(name);
		}
		return null;
	}

	public World getLobbyWorld() {
		return getServer().getWorlds().get(0);
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		setupIcons();
		loadConfig();
		setupCommands();
		setupIcons();
		setupScoreboards();
		getBanList();
		setupLobbyWorld();
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

	private void setupLobbyWorld() {
		World world = getLobbyWorld();
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(800);
		world.setPVP(false);
	}

	private void setupIcons() {
		saveResource("icons/phase-setup.png", false);
		saveResource("icons/phase-pregame.png", false);
		saveResource("icons/phase-scatter.png", false);
		saveResource("icons/phase-main.png", false);
		saveResource("icons/phase-endgame.png", false);
		saveResource("icons/nogame.png", false);
		saveResource("goals/beacon.schematic", false);
		try {
			phaseIcons.put(GameSetup.class, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/phase-setup.png")));
			phaseIcons.put(PreGame.class, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/phase-pregame.png")));
			phaseIcons.put(Scatter.class, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/phase-scatter.png")));
			phaseIcons.put(MainGame.class, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/phase-main.png")));
			phaseIcons.put(EndGame.class, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/phase-endgame.png")));
			phaseIcons.put(null, Bukkit.loadServerIcon(new File(getDataFolder(), "icons/nogame.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupCommands() {
		new TeamCommand(this);
		new UHCCommand(this);
		new ScratchCommand(this);
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

		String worldName = getConfig().getString("world-name");
		worldSetup = new WorldSetup(this, worldName);
		for (World w: getServer().getWorlds()) {
			getLogger().info("Found World: " + w.getName());
		}
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

	public Team joinTeam(Player player, String teamName, boolean requireInvitation) throws UHCError {
		Team onTeam = getMainScoreboard().getPlayerTeam(player);
		if (onTeam != null) {
			throw new UHCError("You are already on a team!");
		}
		Team team = getMainScoreboard().getTeam(teamName);
		if (team == null) {
			throw new UHCError("Unknown Team: " + teamName);
		}
		if (requireInvitation) {
			if (!player.hasMetadata("teamInvites")) {
				throw new UHCError("You have not been invited to join " + team.getDisplayName());
			}
			List<String> invites = (List<String>) player.getMetadata("teamInvites").get(0).value();
			if (!invites.contains(team.getName())) {
				throw new UHCError("You have not been invited to join " + team.getDisplayName());
			}
		}
		if (team.getSize() == getTeamSize()) {
			throw new UHCError("That team already has the maximum number of players.");
		}
		team.addPlayer(player);
		Score s = teamScoreboard.getObjective("members").getScore(Bukkit.getOfflinePlayer(team.getName()));
		s.setScore(team.getPlayers().size());
		return team;
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

	public void startGame() {
		game = new Game(this);
		game.addPhase(new PreGame(this));
		game.addPhase(new Scatter(this));
		game.addPhase(new MainGame(this));
		game.addPhase(new EndGame(this));
		game.start();
	}

	public void startSetup(boolean regen) {
		game = new Game(this);
		game.addPhase(new GameSetup(this, regen));
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

	public boolean isParticipant(String name){
		for (Team team: getMainScoreboard().getTeams()) {
			for (OfflinePlayer player: team.getPlayers()) {
				if (player.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public CachedServerIcon getPhaseIcon(Class<? extends Phase> phaseClass) {
		return phaseIcons.get(phaseClass);
	}

	public UHCBanList getBanList() {
		if (banList == null) {
			banList = new UHCBanList(this);
			File file = new File(getDataFolder(), "bans.tsv");
			if (file.exists()) {
				banList.loadFile(file);
			} else {
				getLogger().warning("No bans.tsv found. export this google doc: https://docs.google.com/spreadsheet/ccc?key=0AjACyg1Jc3_GdEhqWU5PTEVHZDVLYWphd2JfaEZXd2c#gid=0");
			}
		}
		return banList;
	}
}
