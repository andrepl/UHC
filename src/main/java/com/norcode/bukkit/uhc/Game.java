package com.norcode.bukkit.uhc;

import com.norcode.bukkit.uhc.phase.EndGame;
import com.norcode.bukkit.uhc.phase.MainGame;
import com.norcode.bukkit.uhc.phase.Phase;
import com.norcode.bukkit.uhc.phase.PreGame;
import me.confuser.barapi.BarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

public class Game extends BukkitRunnable {

	private Phase[] phases;
	private Phase currentPhase;
	private long phaseStartTime;
	private UHC plugin;
	private int phasePtr = -1;

	public Game(UHC plugin) {
		this.plugin = plugin;
		this.initializeGamePhases();
	}

	private void initializeGamePhases() {
		phases = new Phase[] {
			new PreGame(this.plugin),
			new MainGame(this.plugin),
			new EndGame(this.plugin)
		};
	}

	public void start() {
		phasePtr = 0;
		this.setPhase(phases[0]);
		this.runTaskTimer(plugin, 0, 20);
	}

	private void setPhase(Phase phase) {
		if (currentPhase != null) {
			HandlerList.unregisterAll(currentPhase);
			currentPhase.onEnd();
		}
		currentPhase = phase;
		phaseStartTime = System.currentTimeMillis();
		plugin.getServer().getPluginManager().registerEvents(currentPhase, plugin);
		currentPhase.onStart();
	}

	public void nextPhase() {
		phasePtr ++;
		if (phasePtr > phases.length -1) {
		    gameOver();
		}
		setPhase(phases[phasePtr]);
	}

	private void gameOver() {
		plugin.getLogger().info("Game Over!");
		this.cancel();

	}

	public long getElapsedTime() {
		return System.currentTimeMillis() - phaseStartTime;
	}

	@Override
	public void run() {
		if (getElapsedTime() > currentPhase.getDuration()) {
			nextPhase();
		}
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			int pct = getTimePercentageRemaining();
			plugin.getLogger().info("Phase Pct: " + pct);
			BarAPI.setMessage(p, currentPhase.formatMessage(this), (float)pct);
		}
	}

	public Phase getCurrentPhase() {
		return currentPhase;
	}

	public int getTimePercentageRemaining() {
		return 100 - (int) ((getElapsedTime() / (double) currentPhase.getDuration()) * 100);
	}
}
