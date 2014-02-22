package com.norcode.bukkit.uhc.command;

import com.norcode.bukkit.uhc.Kit;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UHCConfigCommand extends BaseCommand {
	public UHCConfigCommand(JavaPlugin plugin) {
		super(plugin, "config", new String[] {"cfg", "conf"}, "uhc.commands.uhc.config", new String[] { "Set UHC Config Options" });
		registerSubcommand(new SizeCommand(plugin));
		registerSubcommand(new SeedCommand(plugin));
		registerSubcommand(new KitCommand(plugin));
	}

	public static class SizeCommand extends BaseCommand {

		public SizeCommand(JavaPlugin plugin) {
			super(plugin, "size", null, "uhc.commands.uhc.config.size", null);
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			ConfigurationSection cfg = plugin.getConfig();
			if (args.size() == 0) {
				int sizeX = cfg.getInt("size.x");
				int sizeZ = cfg.getInt("size.z");
				commandSender.sendMessage("Current map size is: " + sizeX + "x" + sizeZ);
			} else if (args.size() == 1) {
				int val;
				try {
					val = Integer.parseInt(args.peek());
				} catch (IllegalArgumentException ex) {
					throw new CommandError("Invalid size: " + args.peek());
				}
				cfg.set("size.x", val);
				cfg.set("size.z", val);
				commandSender.sendMessage("Current map size is: " + val + "x" + val);
				plugin.saveConfig();
			} else if (args.size() == 2) {
				int sizeX;
				int sizeZ;
				try {
					sizeX = Integer.parseInt(args.peek());
				} catch (IllegalArgumentException ex) {
					throw new CommandError("Invalid size: " + args.peek());
				}
				args.pop();
				try {
					sizeZ = Integer.parseInt(args.peek());
				} catch (IllegalArgumentException ex) {
					throw new CommandError("Invalid size: " + args.peek());
				}
				cfg.set("size.x", sizeX);
				cfg.set("size.z", sizeZ);
				commandSender.sendMessage("Current map size is: " + sizeX + "x" + sizeZ);
				plugin.saveConfig();
			}
		}
	}

	public static class SeedCommand extends BaseCommand {

		public SeedCommand(JavaPlugin plugin) {
			super(plugin, "seed", null, "uhc.commands.uhc.config.seed", null);
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (args.size() > 0) {
				String seedString = StringUtils.join(" ", args);
				long seed;
				try {
					seed = Long.parseLong(seedString);
				} catch (IllegalArgumentException ex) {
					seed = seedString.hashCode();
				}
				plugin.getConfig().set("seed", seed);
			}
			commandSender.sendMessage("UHC World Seed is " + plugin.getConfig().getString("world-seed"));
		}
	}

	public static class KitCommand extends BaseCommand {
		public KitCommand(JavaPlugin plugin) {
			super(plugin, "kit", null, "uhc.commands.uhc.config.kit", null);
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (args.size() == 0) {
				Kit kit = Kit.fromConfigSection(plugin.getConfig().getConfigurationSection("starter-kit"));
				commandSender.sendMessage("Helmet: " + kit.getHelmet());
				commandSender.sendMessage("Chestplate: " + kit.getChestplate());
				commandSender.sendMessage("Leggings: " + kit.getLeggings());
				commandSender.sendMessage("Boots: " + kit.getBoots());
				String items = "";
				for (ItemStack s: kit.getInventory()) {
					if (s != null) {
						items += CraftItemStack.asNMSCopy(s).getName() + ", ";
					}
				}
				if (items.endsWith(", ")) {
					items = items.substring(0, items.length()-2);
				}
				commandSender.sendMessage("Items: " + items);
			} else if (args.peek().equalsIgnoreCase("set")) {
				Player p = (Player) commandSender;
				plugin.getConfig().set("starter-kit", null);
				ConfigurationSection cfg = plugin.getConfig().createSection("starter-kit");
				cfg.set("helmet", p.getInventory().getHelmet());
				cfg.set("chestplate", p.getInventory().getChestplate());
				cfg.set("leggings", p.getInventory().getLeggings());
				cfg.set("boots", p.getInventory().getBoots());
				List<ItemStack> inv = new ArrayList<ItemStack>();
				for (int i=0; i<p.getInventory().getSize(); i++) {
					inv.add(p.getInventory().getItem(i));
				}
				cfg.set("inventory", inv);
				plugin.saveConfig();
				commandSender.sendMessage("Starter Kit has been copied from " + commandSender.getName());
			} else if (args.peek().equalsIgnoreCase("test")) {
				Kit kit = Kit.fromConfigSection(plugin.getConfig().getConfigurationSection("starter-kit"));
				kit.give((Player) commandSender);
			}
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			List<String> results = new ArrayList<String>();
			if ("set".startsWith(args.peek().toLowerCase())) {
				results.add("set");
			}
			if ("test".startsWith(args.peek().toLowerCase())) {
				results.add("test");
			}
			return results;
		}
	}
}
