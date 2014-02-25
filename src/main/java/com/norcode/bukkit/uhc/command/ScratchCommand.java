package com.norcode.bukkit.uhc.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.LinkedList;

public class ScratchCommand extends BaseCommand {
	public ScratchCommand(JavaPlugin plugin) {
		super(plugin, "scratch", null, "uhc.staff", null);
		plugin.getCommand("scratch").setExecutor(this);
	}
	public static double PI2 = Math.PI * 2;
	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		Player player = (Player) commandSender;
		player.sendMessage("Slice: " + getSlice(player.getLocation()) + ", WorldSpawn: " + player.getLocation().getWorld().getSpawnLocation());

	}

	public int getSlice(Location l) {
		Location spawn = l.getWorld().getSpawnLocation();
		Vector v = l.toVector().subtract(spawn.toVector());
		Vector v2 = new Vector(spawn.getBlockX() + 1000000, spawn.getBlockY(), spawn.getBlockZ()).subtract(spawn.toVector());
		float angle = v.angle(v2);
		if (l.getBlockZ() < spawn.getBlockZ()) {
			angle = -angle;
		}
		if (angle < 0) {
			angle = ((float) Math.PI) + ((float) Math.PI - Math.abs(angle));
		}
		float sliceSize = (float) (2 * Math.PI / 16f);
		return (int) (angle / sliceSize);
	}

}
