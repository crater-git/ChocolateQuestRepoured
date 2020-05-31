package com.teamcqr.chocolatequestrepoured.structuregen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.teamcqr.chocolatequestrepoured.CQRMain;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonBase;
import com.teamcqr.chocolatequestrepoured.util.data.FileIOUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

public class DungeonDataManager {
	
	private static final Map<World, DungeonDataManager> INSTANCES = new HashMap<>();
	
	private boolean modifiedSinceLastSave = false;
	private Map<String, Set<BlockPos>> dungeonData = new HashMap<>();
	protected final String DATA_FILE_NAME = "structures.nbt";
	private File file;

	public static void handleWorldLoad(World world) {
		if(isWorldValid(world) && !INSTANCES.containsKey(world)) {
			createInstance(world);
			getInstance(world).readData();
		}
	}
	
	public static void handleWorldUnload(World world) {
		if(isWorldValid(world)) {
			deleteInstance(world);
		}
	}
	
	public static void handleWorldSave(World world) {
		if(isWorldValid(world)) {
			getInstance(world).saveData();
		}
	}
	
	public static void addDungeonEntry(World world, DungeonBase dungeon, BlockPos position) {
		if(isWorldValid(world)) {
			getInstance(world).insertDungeonEntry(dungeon.getDungeonName(), position);
		}
	}
	
	@Nullable
	public static DungeonDataManager getInstance(World world) {
		if(isWorldValid(world)) {
			return INSTANCES.get(world);
		}
		return null;
	}
	
	private static boolean isWorldValid(World world) {
		return world != null && !world.isRemote;
	}
	
	private static void createInstance(World world) {
		if(isWorldValid(world) && !INSTANCES.containsKey(world)) {
			INSTANCES.put(world, new DungeonDataManager(world));
		}
	}
	
	private static void deleteInstance(World world) {
		if(isWorldValid(world) && INSTANCES.containsKey(world)) {
			INSTANCES.remove(world);
		}
	}
	
	public static Set<String> getSpawnedDungeonNames(World world) {
		return getInstance(world).getSpawnedDungeonNames();
	}
	
	private Set<String> getSpawnedDungeonNames() {
		return dungeonData.keySet();
	}
	
	public static Set<BlockPos> getLocationsOfDungeon(World world, String dungeon) {
		return getInstance(world).getLocationsOfDungeon(dungeon);
	}

	private Set<BlockPos> getLocationsOfDungeon(String dungeon) {
		return dungeonData.getOrDefault(dungeon, new HashSet<>());
	}

	public DungeonDataManager(World world) {
		if(world instanceof ServerWorld) {
			int dim = world.getDimension().getType().getId();
			String path = ((ServerWorld)world).getSaveHandler().getWorldDirectory().getAbsolutePath();
			if (dim == 0) {
				path += "/data/CQR/";
			} else {
				path += "/DIM" + dim + "/data/CQR/";
			}
			this.file = FileIOUtil.getOrCreateFile(path, DATA_FILE_NAME);
		}
	}
	
	public void insertDungeonEntry(String dungeon, BlockPos location) {
		Set<BlockPos> spawnedLocs = dungeonData.getOrDefault(dungeon, new HashSet<>());
		if(spawnedLocs.add(location)) {
			dungeonData.put(dungeon, spawnedLocs);
			if(!modifiedSinceLastSave) {
				modifiedSinceLastSave = true;
			}
		}
	}
	
	public void saveData() {
		if(modifiedSinceLastSave) {
			this.file.delete();
			try {
				if(!this.file.createNewFile()) {
					CQRMain.logger.warn("Unable to create file: " + this.file.getAbsolutePath() + "! Information about dungeons may be lost!");
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			CompoundNBT root = new CompoundNBT();
			ListNBT dungeonNames = FileIOUtil.getOrCreateTagList(root, "dungeons", Constants.NBT.TAG_STRING);
			for(Map.Entry<String, Set<BlockPos>> data : this.dungeonData.entrySet()) {
				if(!data.getValue().isEmpty()) {
					ListNBT locs = FileIOUtil.getOrCreateTagList(root, "dun-" + data.getKey(), Constants.NBT.TAG_COMPOUND);
					for(BlockPos loc : data.getValue()) {
						locs.add(NBTUtil.writeBlockPos(loc));
					}
					dungeonNames.add(StringNBT.valueOf(data.getKey()));
				}
			}
			FileIOUtil.saveNBTCompoundToFile(root, file);
			modifiedSinceLastSave = false;
		}
	}
	
	public void readData() {
		CompoundNBT root = FileIOUtil.getRootNBTTagOfFile(file);
		ListNBT dungeons = FileIOUtil.getOrCreateTagList(root, "dungeons", Constants.NBT.TAG_STRING);
		dungeons.forEach(new Consumer<INBT>() {

			@Override
			public void accept(INBT t) {
				if(t instanceof StringNBT) {
					StringNBT tag = (StringNBT) t;
					String s = tag.getString();
					Set<BlockPos> poss = dungeonData.getOrDefault(s, new HashSet<>());
					ListNBT data = FileIOUtil.getOrCreateTagList(root, "dun-" + s, Constants.NBT.TAG_COMPOUND);
					data.forEach(new Consumer<INBT>() {
						public void accept(INBT t1) {
							if(t1 instanceof CompoundNBT) {
								CompoundNBT tag1 = (CompoundNBT) t1;
								poss.add(NBTUtil.readBlockPos(tag1));
							}
						}
					});
					dungeonData.put(s, poss);
				}
			}
		});
	}

	public boolean isDungeonSpawnLimitMet(DungeonBase dungeon) {
		if (dungeon.getSpawnLimit() < 0) {
			return false;
		}
		if(dungeonData.isEmpty()) {
			return false;
		}
		Set<BlockPos> spawnedLocs = dungeonData.getOrDefault(dungeon.getDungeonName(), new HashSet<>());
		if(spawnedLocs.isEmpty()) {
			return false;
		}
		return spawnedLocs.size() >= dungeon.getSpawnLimit();
	}


}
