package com.norcode.bukkit.uhc;

import com.wimbli.WorldBorder.BorderData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

public class WallBuilder extends BukkitRunnable {
	private int step;
	private int maxSteps;
	private UHC plugin;
	private BorderData borderData;
	private int minX;
	private int maxX;
	private int minZ;
	private int maxZ;
	private World world;

	public WallBuilder(UHC plugin, World world) {
		this.plugin = plugin;
		borderData = this.plugin.getWorldSetup().getBorderData();
		plugin.getLogger().info(borderData.toString());
		minX = (int) borderData.getX() - borderData.getRadiusX();
		maxX = (int) borderData.getX() + borderData.getRadiusX();
		minZ = (int) borderData.getZ() - borderData.getRadiusZ();
		maxZ = (int) borderData.getZ() + borderData.getRadiusZ();
		this.maxSteps = (maxX - minX) + (maxZ - minZ);
		this.step = 0;
		this.world = world;
		plugin.getLogger().info("WallBuilder Initialized: (" + minX + "," + minZ + ") -> (" + maxX + "," + maxZ + ")");
	}

	private void setColumn(int x, int z) {

		BlockState state;
		Location loc = new Location(world, x, 64, z);
		if (!loc.getChunk().isLoaded()) {
			loc.getChunk().load(true);
		}
		for (int y=0; y < world.getMaxHeight(); y++) {
			loc = new Location(world, x, y, z);
			state = loc.getBlock().getState();
			state.setType(Material.BEDROCK);
			state.update(true, false);
		}
	}

	@Override
	public void run() {
		for (int i=0; i<5; i++) {
			//plugin.getLogger().info("Step: " + this.step + "/" + this.maxSteps);
			if (this.step <= maxX - minX) {
				// doing the X lines.
				setColumn(minX + this.step, minZ);
				setColumn(minX + this.step, maxZ);
			} else {
				// doing the X lines.
				int _step = this.step - (maxX - minX);
				setColumn(minX, minZ + _step);
				setColumn(maxX, minZ + _step);
			}
			this.step ++;
			if (this.step == maxSteps) {
				this.cancel();
				plugin.getLogger().info("Walls Complete");
				return;
			}
		}
	}

	public int getPercentageComplete() {
		return (int) ((this.step / (double) this.maxSteps) * 100);
	}

	public void start() {
		this.runTaskTimer(plugin, 1, 1);
	}
}
