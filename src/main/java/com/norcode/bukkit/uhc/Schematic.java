package com.norcode.bukkit.uhc;

import com.google.common.io.Files;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Schematic {

	private int width; // X
	private int length; // Z
	private int height; // Y

	Map<BlockVector, NbtFactory.NbtCompound> tileEntities;
	MaterialData[] blocks;

	public int getWidth() {
		return width;
	}

	public int getLength() {
		return length;
	}

	public int getHeight() {
		return height;
	}

	public Map<BlockVector, NbtFactory.NbtCompound> getTileEntities() {
		return tileEntities;
	}

	public MaterialData[] getBlocks() {
		return blocks;
	}

	private Schematic(NbtFactory.NbtCompound tag) {
		width = tag.getShort("Width", (short) 0);
		length = tag.getShort("Length", (short) 0);
		height = tag.getShort("Height", (short) 0);

		byte[] blockIds = tag.getByteArray("Blocks", null);
		byte[] data = tag.getByteArray("Data", null);
		byte[] addId = new byte[0];
		short[] blockShorts = new short[blockIds.length];
		blocks = new MaterialData[blockShorts.length];

		if (tag.containsKey("AddBlocks")) {
			addId = tag.getByteArray("AddBlocks", null);
		}

		// Combine the AddBlocks data with the first 8-bit block ID
		for (int index = 0; index < blockIds.length; index++) {
			if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
				blockShorts[index] = (short) (blockIds[index] & 0xFF);
			} else {
				if ((index & 1) == 0) {
					blockShorts[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
				} else {
					blockShorts[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
				}
			}
		}

		// Need to pull out tile entities
		NbtFactory.NbtList tileEntityList = tag.getList("TileEntities", false);
		 tileEntities = new HashMap<BlockVector, NbtFactory.NbtCompound>();

		NbtFactory.NbtCompound cTag;
		for (int index=0;index<tileEntityList.size();index++) {
			cTag = (NbtFactory.NbtCompound) tileEntityList.get(index);
			int x = cTag.getInteger("x", 0);
			int y = cTag.getInteger("y", 0);
			int z = cTag.getInteger("z", 0);
			tileEntities.put(new BlockVector(x,y,z), cTag);
		}

		int idx;
		BlockVector vec;
		for (int i=0; i<blockShorts.length; i++) {
			blocks[i] = new MaterialData(blockShorts[i], data[i]);
		}
	}

	public static Schematic fromFile(File file) throws IOException {
		NbtFactory.NbtCompound tag = NbtFactory.fromStream(Files.newInputStreamSupplier(file), NbtFactory.StreamOptions.GZIP_COMPRESSION);
		return new Schematic(tag);
	}
}
