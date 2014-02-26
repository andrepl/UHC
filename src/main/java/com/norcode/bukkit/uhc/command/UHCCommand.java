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
		registerSubcommand(new SetupCommand(plugin));
		registerSubcommand(new UHCConfigCommand(plugin));
		plugin.getCommand("uhc").setExecutor(this);
	}

	public static class SetupCommand extends BaseCommand {
		public SetupCommand(JavaPlugin plugin) {
			super(plugin, "setup", null, "uhc.commands.uhc.setup", new String[] {"start the map generation phase."});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Game game = ((UHC) plugin).getGame();
			boolean regen = false;
			if (args.size() > 0) {
				if ("generate".startsWith(args.peek())) {
					regen = true;
				}
			}
			if (game == null) {
				commandSender.sendMessage("Generating " + plugin.getConfig().getString("world-name"));
				((UHC) plugin).getWorldSetup().start(regen);
			} else if (game.isGameSetup()) {
				throw new CommandError("Setup is already in progress.");
			} else {
				throw new CommandError("Game is already in progress.");
			}
		}
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
