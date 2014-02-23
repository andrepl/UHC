package com.norcode.bukkit.uhc;

import net.minecraft.util.org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UHCBanList {

	public boolean isPlayerBanned(Player player) {
		return bans.containsKey(player.getName()) &&
				bans.get(player.getName()).getExpiry().before(new Date());
	}

	public Ban getBan(Player player) {
		return bans.get(player.getName());
	}

	public static class Ban {
		private String playerName;
		private String reason;
		private Date bannedAt;
		private Date expiry;

		private Ban(String playerName, String reason, Date bannedAt, Date expiry) {
			this.playerName = playerName;
			this.reason = reason;
			this.bannedAt = bannedAt;
			this.expiry = expiry;
		}

		private String getPlayerName() {
			return playerName;
		}

		private String getReason() {
			return reason;
		}

		private Date getBannedAt() {
			return bannedAt;
		}

		public Date getExpiry() {
			return expiry;
		}

		public String getExpiryString() {
			return dateFormat.format(getExpiry());
		}
	}

	private UHC plugin;
	private HashMap<String, Ban> bans;

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("M d, y");

	public UHCBanList(UHC plugin) {
		this.plugin = plugin;
		this.bans = new HashMap<String, Ban>();
	}

	private Date parseDate(String ds) {
		try {
			return dateFormat.parse(ds);
		} catch (ParseException e) {
			return null;
		}
	}

	public void loadFile(File file) {
		FileInputStream fis = null;
		bans.clear();
		try {
			fis = new FileInputStream(file);
			List<String> lines = IOUtils.readLines(fis);
			for (String line: lines) {
				String[] parts = line.split("\\t");
				bans.put(parts[0], new Ban(parts[0], parts[1], parseDate(parts[2]), parseDate(parts[4])));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
