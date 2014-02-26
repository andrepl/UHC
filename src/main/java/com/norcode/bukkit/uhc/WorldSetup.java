package com.norcode.bukkit.uhc;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldFillTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class WorldSetup extends BukkitRunnable {

	private UHC plugin;
	private Goal goal;
	private String worldName;
	private boolean generationStarted = false;
	private boolean complete = false;
	private WallBuilder wallBuilder;
	private Random random = new Random();
	private FakePlayer fakePlayer;
	private boolean regenerate;
	private Method reportMethod;

	public WorldSetup(UHC plugin, String worldName) {
		this.fakePlayer = new FakePlayer(this);
		this.plugin = plugin;
		this.worldName = worldName;
		try {
			reportMethod = WorldFillTask.class.getDeclaredMethod("reportProgress");
			reportMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	public boolean doRegeneration() {
		return regenerate;
	}

	public void setDoRegeneration(boolean doRegeneration) {
		this.regenerate = doRegeneration;
	}

	private void setupGoal() {
		File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("goal-schematic"));
		Schematic schematic = null;
		try {
			schematic = Schematic.fromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		goal = new Goal(schematic);
		World world = plugin.getUHCWorld();
		Location loc = world.getSpawnLocation();
		loc = new Location(world, loc.getBlockX(), world.getHighestBlockYAt(loc), loc.getBlockZ());
		goal.setLocation(loc);
	}

	public Goal getGoal() {
		return goal;
	}

	private char[] chars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
									   'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
									   'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
									   'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
									   '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '?', '.'};

	private String randomString(int size) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<size; i++) {
			sb.append(chars[random.nextInt(chars.length)]);
		}
		return sb.toString();
	}

	public void start(boolean regenerate) {
		this.regenerate = regenerate;
		WorldCreator creator = new WorldCreator(worldName);
		creator.type(WorldType.valueOf(plugin.getConfig().getString("world-type", "NORMAL")));
		creator.environment(World.Environment.valueOf(plugin.getConfig().getString("environment", "NORMAL")));
		creator.generateStructures(plugin.getConfig().getBoolean("generate-structures"));
		if (regenerate) {
			long seed;
			String seedString = plugin.getConfig().getString("world-seed", null);
			if (seedString == null) {
				seedString = randomString(64);
			}
			try {
				seed = Long.parseLong(seedString);
			} catch (IllegalArgumentException ex) {
				seed = seedString.hashCode();
				plugin.getConfig().set("world-seed", seed);
				plugin.saveConfig();
			}
			creator.seed(seed);
		}
		World world = creator.createWorld();
		setupGoal();
		int sizeX = plugin.getConfig().getInt("size.x");
		int sizeZ = plugin.getConfig().getInt("size.z");
		setupBorder(sizeX, sizeZ);
		if (regenerate) {
			startWorldGeneration();
		} else {
			wallBuilder = new WallBuilder(plugin, plugin.getServer().getWorld(worldName));
			wallBuilder.start();
		}
		this.runTaskTimer(plugin, 20, 20);
	}


	private void setupBorder(int sizeX, int sizeZ) {
		World world = plugin.getServer().getWorld(worldName);
		Location spawnLoc = world.getSpawnLocation();
		Config.setBorder(worldName, new BorderData(spawnLoc.getBlockX(), spawnLoc.getBlockZ(), sizeX/2, sizeZ/2, false, false));
		Config.save(true);
	}

	public BorderData getBorderData() {
		return Config.Border(worldName);
	}

	private void startWorldGeneration() {
		generationStarted = true;
		Config.fillTask = new WorldFillTask(plugin.getServer(), fakePlayer, worldName, 208, 10, 1, true);
		if (Config.fillTask.valid()) {
			int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin.getWorldBorder(), Config.fillTask, 1, 1);
			Config.fillTask.setTaskID(task);
		}
	}

	public boolean generationComplete() {
		if (regenerate) {
			return fakePlayer.getFillStatus().equals(FakePlayer.FILL_COMPLETE);
		}
		return true;
	}

	public int getGenerationPercentage() {
		return fakePlayer.getFillPercentage();
	}


	@Override
	public void run() {
		if (regenerate) {
			if (reportMethod != null) {
				try {
					reportMethod.invoke(Config.fillTask);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if (generationComplete()) {
			if (wallBuilder == null) {
				wallBuilder = new WallBuilder(plugin, plugin.getServer().getWorld(worldName));
				wallBuilder.start();
				return;
			}
			if (borderComplete()) {
				this.cancel();
				this.goal.build();
				plugin.getLogger().info("World Setup is complete.");
				complete = true;
				return;
			}
		}
	}

	private boolean borderComplete() {
		return wallBuilder.getPercentageComplete() == 100;
	}

	public int getPercentage() {
		if (regenerate) {
			if (generationComplete()) {
				if (wallBuilder == null) {
					wallBuilder = new WallBuilder(plugin, plugin.getServer().getWorld(worldName));
					wallBuilder.start();
					return 85;
				}
				int pct = 85;
				pct += (int) (getBorderPercentage() * 0.15);
				return pct;
			} else {
				return (int) (getGenerationPercentage() * 0.85);
			}
		} else {
			return getBorderPercentage();
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public int getBorderPercentage() {
		return wallBuilder.getPercentageComplete();
	}
}
