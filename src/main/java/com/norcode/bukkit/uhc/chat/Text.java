package com.norcode.bukkit.uhc.chat;

import net.minecraft.server.v1_7_R1.ChatClickable;
import net.minecraft.server.v1_7_R1.ChatComponentText;
import net.minecraft.server.v1_7_R1.ChatHoverable;
import net.minecraft.server.v1_7_R1.EnumChatFormat;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;


public class Text extends ChatComponentText {

	public Text append(String text) {
		return (Text) a(text);
	}

	public Text append(IChatBaseComponent node) {
		return (Text) a(node);
	}


	public Text append(IChatBaseComponent... nodes) {
		for (IChatBaseComponent node : nodes) {
			a(node);
		}
		return this;
	}

	public Text appendItem(ItemStack stack) {
		net.minecraft.server.v1_7_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound tag = new NBTTagCompound();
		nms.save(tag);
		return append(new Text(nms.getName()).setColor(ChatColor.getByChar(nms.w().e.getChar())).setHover(HoverAction.SHOW_ITEM, new ChatComponentText(tag.toString())));
	}

	public net.minecraft.server.v1_7_R1.ChatModifier getChatModifier() {
		return b();
	}

	public Text setBold(boolean bold) {
		getChatModifier().setBold(bold);
		return this;
	}

	public Text setItalic(boolean italic) {
		getChatModifier().setItalic(italic);
		return this;
	}

	public Text setUnderline(boolean underline) {
		getChatModifier().setUnderline(underline);
		return this;
	}

	public Text setRandom(boolean random) {
		getChatModifier().setRandom(random);
		return this;
	}

	public Text setStrikethrough(boolean strikethrough) {
		getChatModifier().setStrikethrough(strikethrough);
		return this;
	}

	public Text setColor(ChatColor color) {
		if (color != null) {
			getChatModifier().setColor(EnumChatFormat.valueOf(color.name()));
		}
		return this;
	}

	public Text setClick(ClickAction action, String value) {
		getChatModifier().a(new ChatClickable(action.getNMS(), value));
		return this;
	}

	public Text setHover(HoverAction action, IChatBaseComponent value) {
		getChatModifier().a(new ChatHoverable(action.getNMS(), value));
		return this;
	}

	public Text setHoverText(String text) {
		return setHover(HoverAction.SHOW_TEXT, new Text(text));
	}

	public Text setHoverText(String[] text) {
		ItemStack stack = new ItemStack(Material.DIRT);
		ItemMeta meta = stack.getItemMeta();

		List<String> lines = Arrays.asList(text);
		String line1 = lines.remove(0);
		meta.setDisplayName(line1);
		meta.setLore(lines);
		stack.setItemMeta(meta);
		net.minecraft.server.v1_7_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound tag = new NBTTagCompound();
		nms.save(tag);
		setHover(HoverAction.SHOW_ITEM, new ChatComponentText(tag.toString()));
		return this;
	}

	public Text(String s) {
		super(s);
	}

	@Override
	public IChatBaseComponent f() {
		return h();
	}

	public void sendTo(CommandSender p) {
		if (!(p instanceof Player)) {
			p.sendMessage(this.toPlainText());
		} else {
			PacketPlayOutChat packet = new PacketPlayOutChat(this, true);
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}

	private String toPlainText() {
		return e();
	}


}
