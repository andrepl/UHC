package com.norcode.bukkit.uhc;

import net.minecraft.server.v1_7_R1.DataWatcher;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameTimer extends BukkitRunnable {
	private Iterator<Map.Entry<String, String>> cursor;
	private HashMap<String, String> registeredPlayers = new HashMap<String, String>();
	public static final int ENTITY_ID = 1234;
	public static final int MAX_HEALTH = 300;
	private JavaPlugin plugin;
	private String message = null;
	private int value = 300;
	private Location mobLocation;

	public GameTimer(JavaPlugin plugin) {
		this.plugin = plugin;
		mobLocation = new Location(plugin.getServer().getWorlds().get(0), 0, 255, 0);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void clear() {
		this.message = null;
	}

	//Accessing packets
	@SuppressWarnings("deprecation")
	public static PacketPlayOutSpawnEntityLiving getMobPacket(String text, Location loc){
		PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();
		try {
			spawnFieldA.set(mobPacket, (int) ENTITY_ID);
			spawnFieldB.set(mobPacket, (byte) EntityType.WITHER.getTypeId());
			spawnFieldC.set(mobPacket, (int) 0);
			spawnFieldD.set(mobPacket, (int) 255);
			spawnFieldE.set(mobPacket, (int) 0);
			spawnFieldF.set(mobPacket, (byte) 0);
			spawnFieldG.set(mobPacket, (byte) 0);
			spawnFieldH.set(mobPacket, (byte) 0);
			spawnFieldI.set(mobPacket, (byte) 0);
			spawnFieldJ.set(mobPacket, (byte) 0);
			spawnFieldK.set(mobPacket, (byte) 0);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}

		DataWatcher watcher = getWatcher(text, MAX_HEALTH);

		try{
			spawnFieldL.set(mobPacket, watcher);
		} catch(Exception ex){
			ex.printStackTrace();
		}

		return mobPacket;
	}

	public static PacketPlayOutEntityDestroy getDestroyEntityPacket(){
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy();

		try {
			destroyFieldA.set(packet, new int[]{ENTITY_ID});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public static PacketPlayOutEntityMetadata getMetadataPacket(DataWatcher watcher){
		PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();

		try {
			metaFieldA.set(metaPacket, (int) ENTITY_ID);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}

		try{
			metaFieldB.set(metaPacket, watcher.c());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return metaPacket;
	}


	public static DataWatcher getWatcher(String text, int health){
		DataWatcher watcher = new DataWatcher(null);

		watcher.a(0, (Byte) (byte) 0x20); //Flags, 0x20 = invisible
		watcher.a(6, (Float) (float) health);
		watcher.a(10, (String) text); //Entity name
		watcher.a(11, (Byte) (byte) 1); //Show name, 1 = show, 0 = don't show
		//watcher.a(16, (Integer) (int) health); //Wither health, MAX_HEALTH = full health

		return watcher;
	}


	public static void sendPacket(Player player, Packet packet) {
		Bukkit.getServer().getLogger().info("Sending " + packet.getClass() + " to " + player);
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		entityPlayer.playerConnection.sendPacket(packet);
	}

	public static Field getField(Class<?> cl, String field_name){
		try {
			Field field = cl.getDeclaredField(field_name);
			return field;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Field spawnFieldA;
	private static Field spawnFieldB;
	private static Field spawnFieldC;
	private static Field spawnFieldD;
	private static Field spawnFieldE;
	private static Field spawnFieldF;
	private static Field spawnFieldG;
	private static Field spawnFieldH;
	private static Field spawnFieldI;
	private static Field spawnFieldJ;
	private static Field spawnFieldK;
	private static Field spawnFieldL;

	private static Field destroyFieldA;

	private static Field metaFieldA;
	private static Field metaFieldB;

	public static void reflect() {
		spawnFieldA = getField(PacketPlayOutSpawnEntityLiving.class, "a");
		spawnFieldB = getField(PacketPlayOutSpawnEntityLiving.class, "b");
		spawnFieldC = getField(PacketPlayOutSpawnEntityLiving.class, "c");
		spawnFieldD = getField(PacketPlayOutSpawnEntityLiving.class, "d");
		spawnFieldE = getField(PacketPlayOutSpawnEntityLiving.class, "e");
		spawnFieldF = getField(PacketPlayOutSpawnEntityLiving.class, "f");
		spawnFieldG = getField(PacketPlayOutSpawnEntityLiving.class, "g");
		spawnFieldH = getField(PacketPlayOutSpawnEntityLiving.class, "h");
		spawnFieldI = getField(PacketPlayOutSpawnEntityLiving.class, "i");
		spawnFieldJ = getField(PacketPlayOutSpawnEntityLiving.class, "j");
		spawnFieldK = getField(PacketPlayOutSpawnEntityLiving.class, "k");
		spawnFieldL = getField(PacketPlayOutSpawnEntityLiving.class, "l");
		spawnFieldA.setAccessible(true);
		spawnFieldB.setAccessible(true);
		spawnFieldC.setAccessible(true);
		spawnFieldD.setAccessible(true);
		spawnFieldE.setAccessible(true);
		spawnFieldF.setAccessible(true);
		spawnFieldG.setAccessible(true);
		spawnFieldH.setAccessible(true);
		spawnFieldI.setAccessible(true);
		spawnFieldJ.setAccessible(true);
		spawnFieldK.setAccessible(true);
		spawnFieldL.setAccessible(true);

		destroyFieldA = getField(PacketPlayOutEntityDestroy.class, "a");
		destroyFieldA.setAccessible(true);

		metaFieldA = getField(PacketPlayOutEntityMetadata.class, "a");
		metaFieldA.setAccessible(true);
		metaFieldB = getField(PacketPlayOutEntityMetadata.class, "b");
		metaFieldB.setAccessible(true);


	}

	public void registerPlayer(Player player) {
		if (!registeredPlayers.containsKey(player.getName())) {
			registeredPlayers.put(player.getName(), getMessageHash());
			if (message != null) {
				sendPacket(player, getMobPacket(message, mobLocation));
				sendPacket(player, getMetadataPacket(getWatcher(message, value)));
			}
		}
	}

	public void unregisterPlayer(Player player) {
		if (registeredPlayers.containsKey(player.getName())) {
			registeredPlayers.remove(player.getName());
			if (message != null) {
				sendPacket(player, getDestroyEntityPacket());
			}
		}
	}

	@Override
	public void run() {
		if (registeredPlayers.size() == 0) {
			return;
		}

		if (cursor == null) {
			cursor = registeredPlayers.entrySet().iterator();
		}
		Map.Entry<String, String> entry;
		try {
			if (!cursor.hasNext()) {
				cursor = registeredPlayers.entrySet().iterator();
			}
			entry = cursor.next();
		} catch (ConcurrentModificationException ex) {
			cursor = registeredPlayers.entrySet().iterator();
			entry = cursor.next();
		}
		Player player = plugin.getServer().getPlayerExact(entry.getKey());
		if (entry.getValue() == null && getMessageHash() != null) {
			// show
			Packet packet = getMobPacket(getMessage(), mobLocation);
			sendPacket(player, packet);
		} else if (entry.getValue() != null && getMessageHash() == null) {
			// hide
			Packet packet = getDestroyEntityPacket();
			sendPacket(player, packet);
		}
		if (getMessageHash() != null) {
			sendPacket(player, getMetadataPacket(getWatcher(getMessage(), getValue())));
			if (!entry.getValue().equals(getMessageHash())) {
				entry.setValue(getMessageHash());
				Packet packet = getMetadataPacket(getWatcher(message, value));
				sendPacket(player, packet);
			}
		}
	}

	public String getMessageHash() {
		if (message == null || value < 0) {
			return null;
		}
		return message + "|" + Integer.toString(value);
	}

	public int getValue() {
		return value;
	}
}