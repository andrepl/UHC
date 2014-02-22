package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.Kit;
import com.norcode.bukkit.uhc.UHC;
import com.wimbli.WorldBorder.BorderData;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.LinkedList;
import java.util.Random;

public class Scatter extends Phase {
	private Random rand;
	int teamCount;
	LinkedList<Team> teams;
	boolean xWise;
	int sliceSize;
	long delayUntil = 0;
	BukkitRunnable spawnerTask;
	private Kit starterKit;

	public Scatter(UHC plugin) {
		super(plugin, "Scattering");
		starterKit = Kit.fromConfigSection(plugin.getConfig().getConfigurationSection("starter-kit"));
	}

	@Override
	public void onStart() {
		rand = new Random();
		teams = new LinkedList<Team>(plugin.getMainScoreboard().getTeams());
		teamCount = teams.size();
		BorderData bd = plugin.getWorldSetup().getBorderData();
		xWise = false;
		if (bd.getRadiusX() > bd.getRadiusZ()) {
			xWise = true;
			sliceSize = (bd.getRadiusX() * 2) / teamCount;
		}
		spawnerTask = new BukkitRunnable() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				if (now < delayUntil) {
					return;
				}
				if (teams.size() == 0) {
					plugin.getLogger().info("Spawner Task Finished.");
					this.cancel();
				}

				if (trySpawn()) {
					// spawn Successful add a delay.
					delayUntil = now + 1000;
				}
			}
		};
		spawnerTask.runTaskTimer(plugin, 2, 2);
	}

	private int getIndex() {
		return teamCount - teams.size();
	}

	private Location getPotentialSpawnLocation() {
		int idx = getIndex();
		int minZ = getMinZ();
		int maxZ = getMaxZ();
		int minX = getMinX();
		int maxX = getMaxX();
		if (xWise) {
			minX = getMinX() + (sliceSize * idx);
			maxX = getMaxX() + (sliceSize * (idx+1));
		} else {
			minZ = getMinZ() + (sliceSize * idx);
			maxZ = getMaxZ() + (sliceSize * (idx+1));
		}
		int x = rand.nextInt(maxX - minX) + minX;
		int z = rand.nextInt(maxZ - minZ) + minZ;
		plugin.getLogger().info("PotentialSpawnWorld: " + plugin.getUHCWorld());
		int y = plugin.getUHCWorld().getHighestBlockYAt(x,z);
		return new Location(plugin.getUHCWorld(), x, y, z);
	}


	private static final BlockFace[] eightDirs = new BlockFace[] {
		BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.WEST, BlockFace.EAST,
				BlockFace.SOUTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST
	};

	private boolean isValidSpawn(Location loc) {
		if (loc == null) {
			return false;
		} else {
			Block b = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()).getBlock();
			if (!b.getType().isSolid()) {
				plugin.getLogger().info(b + "is not solid.");
				return false;
			}
			Block footBlock = b.getRelative(BlockFace.UP);
			if (!footBlock.isEmpty()) {
				plugin.getLogger().info(footBlock + "is not empty.");
				return false;
			}
			if (!footBlock
				  .getRelative(BlockFace.UP).isEmpty()) {
				plugin.getLogger().info(footBlock.getRelative(BlockFace.UP) + "is not empty.");
				return false;
			}
			if (!footBlock
					.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty()) {
				plugin.getLogger().info(footBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP) + "is not empty.");

				return false;
			}

			for (BlockFace bf: eightDirs) {
				if (footBlock.getRelative(bf).getType().isSolid()) {
					plugin.getLogger().info(footBlock.getRelative(bf) + "is not empty.");
					return false;
				}
			}
			return true;
		}
	}

	public boolean trySpawn() {
		Location loc = getPotentialSpawnLocation();
		plugin.getLogger().info("Attempting Spawn At: " + loc);
		if (!isValidSpawn(loc)) {
			plugin.getLogger().info(" ... Bad Spawn Location!");
			return false;
		}
		plugin.getLogger().info(" ... Valid! Spawning teammates.");
		loc = loc.add(0.5, 0.1, 0.5);
		Team team = teams.pop();
		for (OfflinePlayer p: team.getPlayers()) {
			plugin.getLogger().info(" ... " + p.getName());
			if (p.isOnline()) {
				teleportPlayer(p.getPlayer(), loc);
			}
		}

		return true;
	}

	private void teleportPlayer(Player player, Location loc) {
		if (!loc.getChunk().isLoaded()) {
			loc.getChunk().load(true);
		}
		player.teleport(loc);
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10));
		starterKit.give(player);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getFrom().getWorld().getUID().equals(plugin.getUHCWorld().getUID())) {
			if (!event.getPlayer().hasPermission("uhc.staff")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerOpenInventory(InventoryOpenEvent event) {
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			event.setCancelled(true);
		}
	}

	private int getMaxX() {
		return (int) plugin.getWorldSetup().getBorderData().getX() + plugin.getWorldSetup().getBorderData().getRadiusX();
	}

	private int getMaxZ() {
		return (int) plugin.getWorldSetup().getBorderData().getZ() + plugin.getWorldSetup().getBorderData().getRadiusZ();
	}

	private int getMinX() {
		return (int) plugin.getWorldSetup().getBorderData().getX() - plugin.getWorldSetup().getBorderData().getRadiusX();
	}

	private int getMinZ() {
		return (int) plugin.getWorldSetup().getBorderData().getZ() - plugin.getWorldSetup().getBorderData().getRadiusZ();
	}

	@Override
	public void onEnd() {
		for (Player p: plugin.getUHCWorld().getPlayers()) {
			p.removePotionEffect(PotionEffectType.BLINDNESS);
			p.setHealth(p.getMaxHealth());
			p.setSaturation(20);
			p.setFoodLevel(20);
		}
	}

	@Override
	public String formatMessage(Game game) {
		return "Scattering Teams.";
	};

	@Override
	public boolean isOver() {
		return teams.isEmpty();
	}

	@Override
	public int getPercentage() {
		return (int) (((teamCount-teams.size()) / (float) teamCount) * 100);
	}
}
