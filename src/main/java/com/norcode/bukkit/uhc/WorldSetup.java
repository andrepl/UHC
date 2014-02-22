package com.norcode.bukkit.uhc;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldFillTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;

public class WorldSetup extends BukkitRunnable {

	private UHC plugin;
	private String worldName;
	private Field reportTotalField;
	private Field reportNumField;
	private Field reportTargetField;
	private boolean generationStarted = false;
	private boolean complete = false;
	private WallBuilder wallBuilder;

	public WorldSetup(UHC plugin, String worldName) {
		this.plugin = plugin;
		this.worldName = worldName;
		reflectWB();
	}

	private void reflectWB() {
		try {
			reportTotalField = WorldFillTask.class.getDeclaredField("reportTotal");
			reportNumField = WorldFillTask.class.getDeclaredField("reportNum");
			reportTargetField = WorldFillTask.class.getDeclaredField("reportTarget");
			reportTotalField.setAccessible(true);
			reportNumField.setAccessible(true);
			reportTargetField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		WorldCreator creator = new WorldCreator(worldName);
		creator.type(WorldType.valueOf(plugin.getConfig().getString("world-type", "NORMAL")));
		creator.environment(World.Environment.valueOf(plugin.getConfig().getString("environment", "NORMAL")));
		creator.generateStructures(plugin.getConfig().getBoolean("generate-structures"));
		long seed;
		String seedString = plugin.getConfig().getString("world-seed");
		try {
			seed = Long.parseLong(seedString);
		} catch (IllegalArgumentException ex) {
			seed = seedString.hashCode();
			plugin.getConfig().set("world-seed", seed);
			plugin.saveConfig();
		}
		creator.seed(seed);
		World world = creator.createWorld();
		int sizeX = plugin.getConfig().getInt("size.x");
		int sizeZ = plugin.getConfig().getInt("size.z");
		setupBorder(sizeX, sizeZ);
		startWorldGeneration();
		this.runTaskTimer(plugin, 20*10, 20*10);
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
		Config.fillTask = new WorldFillTask(plugin.getServer(), null, worldName, 208, 1, 1, true);
		if (Config.fillTask.valid()) {
			int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin.getWorldBorder(), Config.fillTask, 1, 1);
			Config.fillTask.setTaskID(task);
		}
	}

	public boolean generationComplete() {
		boolean genComplete = (Config.fillTask.isPaused() && getGenerationPercentage() == 100);
		plugin.getLogger().info("is Generation Complete? " + genComplete + " " + getGenerationPercentage() + "%");
		return genComplete;
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

	@Override
	public void run() {
		if (generationComplete()) {
			if (wallBuilder == null) {
				wallBuilder = new WallBuilder(plugin, plugin.getServer().getWorld(worldName));
				wallBuilder.start();
				return;
			}
			if (borderComplete()) {
				this.cancel();
				plugin.getLogger().info("World Setup is complete.");
				complete = true;
				return;
			}
		}
		plugin.getLogger().info("World Setup is " + getPercentage() + "% complete.");
	}

	private boolean borderComplete() {
		return wallBuilder.getPercentageComplete() == 100;
	}

	public int getPercentage() {
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
	}

	public boolean isComplete() {
		return complete;
	}

	public int getBorderPercentage() {
		return wallBuilder.getPercentageComplete();
	}
}
