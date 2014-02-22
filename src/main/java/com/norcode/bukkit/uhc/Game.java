package com.norcode.bukkit.uhc;

import com.norcode.bukkit.uhc.phase.EndGame;
import com.norcode.bukkit.uhc.phase.GameSetup;
import com.norcode.bukkit.uhc.phase.MainGame;
import com.norcode.bukkit.uhc.phase.Phase;
import com.norcode.bukkit.uhc.phase.PreGame;
import me.confuser.barapi.BarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Game extends BukkitRunnable {

	private List<Phase> phases = new ArrayList<Phase>();
	private Phase currentPhase;
	private UHC plugin;
	private int phasePtr = -1;

	public Game(UHC plugin) {
		this.plugin = plugin;
	}

	public void addPhase(Phase p) {
		phases.add(p);
	}

	public void start() {
		phasePtr = 0;
		this.setPhase(phases.get(0));
		this.runTaskTimer(plugin, 0, 20);
	}

	private void setPhase(Phase phase) {
		if (currentPhase != null) {
			HandlerList.unregisterAll(currentPhase);
			currentPhase.onEnd();
		}
		currentPhase = phase;
		phase.setStartTime(System.currentTimeMillis());
		plugin.getServer().getPluginManager().registerEvents(currentPhase, plugin);
		currentPhase.onStart();
	}

	public void nextPhase() {
		phasePtr ++;
		if (phasePtr > phases.size() -1) {
		    gameOver();
			return;
		}
		setPhase(phases.get(phasePtr));
	}

	private void gameOver() {
		this.clearBars();
		this.cancel();
		this.currentPhase = null;
		this.phasePtr = -1;
		plugin.gameOver();
	}

	private void clearBars() {
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			BarAPI.removeBar(p);
		}
	}

	@Override
	public void run() {
		if (currentPhase.isOver()) {
			plugin.getLogger().info(currentPhase.getClass().getSimpleName() + " is over.");
			nextPhase();
			return;
		}
		int pct = currentPhase.getPercentage();
		String msg = currentPhase.formatMessage(this);
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			BarAPI.setMessage(p, msg, (float) pct);
		}
	}

	public Phase getCurrentPhase() {
		return currentPhase;
	}


	public boolean isGameSetup() {
		return currentPhase instanceof GameSetup;
	}

	public boolean isPreGame() {
		return currentPhase instanceof PreGame;
	}

	public boolean isMainGame() {
		return currentPhase instanceof MainGame;
	}

	public boolean isEndGame() {
		return currentPhase instanceof EndGame;
	}
}
