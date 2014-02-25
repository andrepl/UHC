package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.Kit;
import com.norcode.bukkit.uhc.UHC;
import com.wimbli.WorldBorder.BorderData;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Scatter extends Phase {
	private Random rand;
	int teamCount;
	LinkedList<Team> teams;
	int sliceCount = 16;
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
		World world = plugin.getUHCWorld();
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(800);
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
					return;
				}

				if (trySpawn()) {
					// spawn Successful add a delay.
					delayUntil = now + 1000;
				}
			}
		};
		spawnerTask.runTaskTimer(plugin, 2, 2);
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

	private ArrayList<Integer> occupiedSlices = new ArrayList<Integer>();

	private boolean isTooClose(int slice) {
		String occ = "Occupied: ";
		for (int i: occupiedSlices) {
			occ += i + ",";
		}
		plugin.getLogger().info(occ);
		if (occupiedSlices.contains(slice)) {
			plugin.getLogger().info("Too Close (slice " + slice + " is already occupied)");
			return true;
		}
		int minEmpty = (16 - teamCount) / teamCount;
		plugin.getLogger().info("Radius: " + minEmpty);
		for (int i=1; i<=minEmpty; i++) {
			int vp = slice+i;
			int vm = slice-i;
			if (vp >= 16) {
				vp = vp - 16;
			}
			if (vm < 0) {
				vm = 16 + vm;
			}

			if (occupiedSlices.contains(vm)) {
				plugin.getLogger().info("Too Close (slice " + slice + " is " + i + " away from " + vm + ")");
				return true;
			} else if (occupiedSlices.contains(vp)) {
				plugin.getLogger().info("Too Close (slice " + slice + " is " + i + " away from " + vp + ")");
				return true;
			}
		}
		return false;
	}

	private Location getPotentialSpawnLocation() {
		int minZ = getMinZ();
		int maxZ = getMaxZ();
		int minX = getMinX();
		int maxX = getMaxX();
		plugin.getLogger().info("Choosing location in (" + minX + "," + minZ + ") - (" + maxX + "," + maxZ + ")");
		World w = plugin.getUHCWorld();
		Location l = null;
		int tries = 0;
		while (tries < 1000) {
			int x = rand.nextInt(maxX - minX) + minX;
			int z = rand.nextInt(maxZ - minZ) + minZ;
			l = new Location(w, x, 64, z);
			Location ws = l.getWorld().getSpawnLocation();
			plugin.getLogger().info("trying " + l.getBlockX() + "," + l.getBlockZ() + " (spawn is " + ws.getBlockX() + "," + ws.getBlockZ() + ")");
			int slice = getSlice(l);
			if (isTooClose(slice)) {
				l = null;
				tries ++;
				continue;
			}
			break;
		}
		if (l != null) {
			plugin.getLogger().info("PotentialSpawnWorld: " + plugin.getUHCWorld());
			int y = plugin.getUHCWorld().getHighestBlockYAt(l);
			l.setY(y);
		}
		return l;

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
			int minDistance = plugin.getWorldSetup().getBorderData().getRadiusX() / 3; // maybe tweak me?

			if (loc.distance(plugin.getUHCWorld().getSpawnLocation()) < minDistance) {
				plugin.getLogger().info(loc + " is too close to spawn!");
				return false;
			}
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
		occupiedSlices.add(getSlice(loc));
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
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 6));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		starterKit.give(player);
	}


	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getPlayer().hasPermission("uhc.staff")) {
			return;
		}
		if (plugin.isParticipant(event.getPlayer().getName())) {
			return;
		}
		event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You can't join a game in progress unless you were registered in pre-game.");
	}

	@EventHandler(ignoreCancelled =true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(plugin.getMainScoreboard());
	}

	@EventHandler(ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event) {
		event.setMotd("UHC In Progress");
		event.setServerIcon(plugin.getPhaseIcon(PreGame.class));
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		if (!event.getPlayer().hasPermission("uhc.staff")) {
			event.setCancelled(true);
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
		plugin.getLogger().info("Scatter onEnd");
		for (Player p: plugin.getUHCWorld().getPlayers()) {
			for (PotionEffect effect: p.getActivePotionEffects()) {
				p.removePotionEffect(effect.getType());
			}
			p.setHealth(p.getMaxHealth());
			p.setSaturation(20);
			p.setFoodLevel(20);
		}
		plugin.getLogger().info("Scatter onEnded");
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
