package com.norcode.bukkit.uhc.command;

import com.norcode.bukkit.uhc.Game;
import com.norcode.bukkit.uhc.UHC;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;

public class UHCCommand extends BaseCommand {
	public UHCCommand(JavaPlugin plugin) {
		super(plugin, "uhc", null, "uhc.commands.uhc", null);
		registerSubcommand(new StartCommand(plugin));
		plugin.getCommand("uhc").setExecutor(this);
	}

	public static class StartCommand extends BaseCommand {

		public StartCommand(JavaPlugin plugin) {
			super(plugin, "start", null, "uhc.commands.uhc.start", new String[] {"Start the game"});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Game game = ((UHC) plugin).getGame();
			if (game != null) {
				throw new CommandError("There is already a game in progress.");
			}
			((UHC) plugin).startGame();
		}
	}

}
