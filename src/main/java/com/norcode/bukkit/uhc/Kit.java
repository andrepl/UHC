package com.norcode.bukkit.uhc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Kit {

	ItemStack helmet;
	ItemStack chestplate;
	ItemStack leggings;
	ItemStack boots;
	List<ItemStack> inventory;

	public Kit() {
	}

	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}

	public void setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
	}

	public void setLeggings(ItemStack leggings) {
		this.leggings = leggings;
	}

	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	public void setInventory(List<ItemStack> inventory) {
		this.inventory = inventory;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public ItemStack getChestplate() {
		return chestplate;
	}

	public ItemStack getLeggings() {
		return leggings;
	}

	public ItemStack getBoots() {
		return boots;
	}

	public List<ItemStack> getInventory() {
		return inventory;
	}

	public void give(Player p) {
		if (helmet != null) {
			p.getInventory().setHelmet(helmet.clone());
		}
		if (chestplate != null) {
			p.getInventory().setChestplate(chestplate.clone());
		}
		if (leggings != null) {
			p.getInventory().setLeggings(leggings.clone());
		}
		if (boots != null) {
			p.getInventory().setBoots(boots.clone());
		}
		for (int i=0; i<inventory.size(); i++) {
			p.getInventory().setItem(i, inventory.get(i));
		}
	}

	public static Kit fromConfigSection(ConfigurationSection cfg) {
		Kit kit = new Kit();
		if (cfg != null) {
			kit.setInventory((List<ItemStack>) cfg.getList("inventory"));
			kit.setHelmet(cfg.getItemStack("helmet", null));
			kit.setChestplate(cfg.getItemStack("chestplate", null));
			kit.setLeggings(cfg.getItemStack("leggings", null));
			kit.setBoots(cfg.getItemStack("boots", null));
		}
		return kit;
	}

	public static Kit fromPlayer(Player p) {
		Kit kit = new Kit();
		kit.setHelmet(p.getInventory().getHelmet());
		kit.setChestplate(p.getInventory().getChestplate());
		kit.setLeggings(p.getInventory().getLeggings());
		kit.setBoots(p.getInventory().getBoots());
		List<ItemStack> inv = new ArrayList<ItemStack>();

		for (int i=0; i<p.getInventory().getSize(); i++) {
			inv.add(p.getInventory().getItem(i).clone());
		}
		kit.setInventory(inv);
		return kit;
	}
}
