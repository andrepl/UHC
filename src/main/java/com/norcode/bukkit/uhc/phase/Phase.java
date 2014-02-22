package com.norcode.bukkit.uhc.phase;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.event.Listener;

import java.util.concurrent.TimeUnit;

public abstract class Phase implements Listener {
	protected long duration;
	protected String name;
	protected String message;
	protected UHC plugin;

	protected Phase(UHC plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		this.message = name;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public abstract void onStart();
	public abstract void onEnd();

	public String formatMessage(Game game) {
		return message;
	}

	public String formatSecondsRemaining(int seconds) {
		int hours = (int) TimeUnit.SECONDS.toHours(seconds);
		seconds -= TimeUnit.HOURS.toSeconds(hours);
		int minutes = (int) TimeUnit.SECONDS.toMinutes(seconds);
		seconds -= TimeUnit.MINUTES.toSeconds(minutes);
		StringBuilder sb = new StringBuilder();
		if (hours > 0) {
			sb.append(hours);
			sb.append(":");
		}
		if (minutes < 10 && hours > 0) {
			sb.append("0");
		}
		sb.append(minutes);
		sb.append(":");
		if (seconds < 10) {
			sb.append("0");
		}
		sb.append(seconds);
		return sb.toString();
	}

}
