package com.norcode.bukkit.uhc;

import net.minecraft.util.org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

public class Goal {
	private Location location;
	private Schematic schematic;
	private boolean isBuilt = false;

	public Goal(Schematic schematic) {
		this.schematic = schematic;
	}

	public Goal(Location location) {
		this.location = location;
	}

	public Goal(Location location, Schematic schematic) {
		this.location = location;
		this.schematic = schematic;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		Validate.isTrue(!(isBuilt),
				"You cannot change the location once the building is placed.");
		this.location = location;
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public void setSchematic(Schematic schematic) {
		this.schematic = schematic;
	}

	public boolean isBuilt() {
		return isBuilt;
	}

	public void build() {
		int width = schematic.getWidth();
		int length = schematic.getLength();
		int height = schematic.getHeight();

		Location origin = location.clone().subtract((width / 2), 0, (length / 2));
		int idx;
		MaterialData md;
		BlockState state;
		for (int x=0;x<width;++x) {
			for (int y=0;y<height;++y) {
				for (int z=0;z<length;++z) {
					idx = y*width*length+z*width+x;
					md = schematic.getBlocks()[idx];
					state = origin.clone().add(x, y, z).getBlock().getState();
					state.setType(md.getItemType());
					state.setData(md);
					state.update(true, false);
				}
			}
		}
	}

	public void dropWalls() {
		int width = schematic.getWidth();
		int length = schematic.getLength();
		int height = schematic.getHeight();
		Location origin = location.clone().subtract((width / 2), 0, (length / 2));
		int idx;
		MaterialData md;
		BlockState state;
		for (int x=0;x<width;++x) {
			for (int y=1;y<height;++y) {
				for (int z=0;z<length;++z) {
					idx = y*width*length+z*width+x;
					md = schematic.getBlocks()[idx];
					if (md.getItemType() == Material.BEDROCK) {
						state = origin.clone().add(x, y, z).getBlock().getState();
						state.setType(Material.AIR);
						state.setRawData((byte) 0);
						state.update(true, false);
					}
				}
			}
		}
	}
}
