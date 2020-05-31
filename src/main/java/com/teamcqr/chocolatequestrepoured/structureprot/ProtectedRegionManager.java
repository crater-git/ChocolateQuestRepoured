package com.teamcqr.chocolatequestrepoured.structureprot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import com.teamcqr.chocolatequestrepoured.CQRMain;
import com.teamcqr.chocolatequestrepoured.network.packets.toClient.SPacketSyncProtectedRegions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ProtectedRegionManager {

	private static ProtectedRegionManager clientInstance = new ProtectedRegionManager(null);
	private static final Map<World, ProtectedRegionManager> instances = new HashMap<>();
	private final Map<UUID, ProtectedRegion> protectedRegions = new HashMap<>();
	private final World world;
	private final File folder;

	public ProtectedRegionManager(World world) {
		this.world = world;
		if (world != null && world instanceof ServerWorld) {
			int dim = world.getDimension().getType().getId();
			if (dim == 0) {
				this.folder = new File(((ServerWorld)world).getSaveHandler().getWorldDirectory(), "data/CQR/protected_regions");
			} else {
				this.folder = new File(((ServerWorld)world).getSaveHandler().getWorldDirectory(), "DIM" + dim + "/data/CQR/protected_regions");
			}
		} else {
			this.folder = null;
		}
	}

	public static void handleWorldLoad(World world) {
		if (!world.isRemote && ProtectedRegionManager.getInstance(world) == null) {
			ProtectedRegionManager.createInstance(world);
			ProtectedRegionManager.getInstance(world).loadData();
		}
	}

	public static void handleWorldSave(World world) {
		if (!world.isRemote) {
			ProtectedRegionManager.getInstance(world).saveData();
		}
	}

	public static void handleWorldUnload(World world) {
		if (!world.isRemote) {
			ProtectedRegionManager.deleteInstance(world);
		}
	}

	@Nullable
	public static ProtectedRegionManager getInstance(World world) {
		if (!world.isRemote) {
			return ProtectedRegionManager.instances.get(world);
		} else {
			return ProtectedRegionManager.clientInstance;
		}
	}

	public static void createInstance(World world) {
		if (!world.isRemote) {
			ProtectedRegionManager.instances.putIfAbsent(world, new ProtectedRegionManager(world));
		}
	}

	public static void deleteInstance(World world) {
		if (!world.isRemote) {
			ProtectedRegionManager.instances.remove(world);
		}
	}

	@Nullable
	public ProtectedRegion getProtectedRegion(UUID uuid) {
		return this.protectedRegions.get(uuid);
	}

	public void addProtectedRegion(ProtectedRegion protectedRegion) {
		if (protectedRegion != null && this.protectedRegions.containsKey(protectedRegion.getUuid())) {
			CQRMain.logger.warn("Protected region with uuid {} already exists.", protectedRegion.getUuid());
		}
		if (protectedRegion != null && !this.protectedRegions.containsKey(protectedRegion.getUuid())) {
			this.protectedRegions.put(protectedRegion.getUuid(), protectedRegion);

			if (this.world != null && !this.world.isRemote) {
				// TODO Only send changes to clients
				CQRMain.NETWORK.sendToDimension(new SPacketSyncProtectedRegions(this.getProtectedRegions()), this.world.provider.getDimension());
			}
		}
	}

	public void removeProtectedRegion(ProtectedRegion protectedRegion) {
		this.removeProtectedRegion(protectedRegion.getUuid());
	}

	public void removeProtectedRegion(UUID uuid) {
		if (this.protectedRegions.containsKey(uuid)) {
			this.protectedRegions.remove(uuid);

			if (this.world != null && !this.world.isRemote) {
				// TODO Only send changes to clients
				CQRMain.NETWORK.sendToDimension(new SPacketSyncProtectedRegions(this.getProtectedRegions()), this.world.provider.getDimension());
			}
		}
	}

	public List<ProtectedRegion> getProtectedRegions() {
		return new ArrayList<>(this.protectedRegions.values());
	}

	public void clearProtectedRegions() {
		this.protectedRegions.clear();
	}

	public void deleteInvalidModules() {
		for (ProtectedRegion protectedRegion : this.getProtectedRegions()) {
			if (!protectedRegion.isValid()) {
				this.protectedRegions.remove(protectedRegion.getUuid());
			}
		}
	}

	private void saveData() {
		if (!this.world.isRemote) {
			if (!this.folder.exists()) {
				this.folder.mkdirs();
			}
			for (File file : FileUtils.listFiles(this.folder, new String[] { "nbt" }, false)) {
				file.delete();
			}
			this.deleteInvalidModules();
			for (ProtectedRegion protectedRegion : this.protectedRegions.values()) {
				this.createFileFromProtectedRegion(this.folder, protectedRegion);
			}
		}
	}

	private void createFileFromProtectedRegion(File folder, ProtectedRegion protectedRegion) {
		File file = new File(folder, protectedRegion.getUuid().toString() + ".nbt");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			try (OutputStream outputStream = new FileOutputStream(file)) {
				CompressedStreamTools.writeCompressed(protectedRegion.writeToNBT(), outputStream);
			}
		} catch (IOException e) {
			CQRMain.logger.info("Failed to save protected region to file: " + file.getName(), e);
		}
	}

	private void loadData() {
		if (!this.world.isRemote) {
			if (!this.folder.exists()) {
				this.folder.mkdirs();
			}
			this.protectedRegions.clear();
			for (File file : FileUtils.listFiles(this.folder, new String[] { "nbt" }, false)) {
				this.createProtectedRegionFromFile(file);
			}
		}
	}

	private void createProtectedRegionFromFile(File file) {
		try (InputStream inputStream = new FileInputStream(file)) {
			CompoundNBT compound = CompressedStreamTools.readCompressed(inputStream);
			ProtectedRegion protectedRegion = new ProtectedRegion(this.world, compound);
			if (!this.protectedRegions.containsKey(protectedRegion.getUuid())) {
				this.protectedRegions.put(protectedRegion.getUuid(), protectedRegion);
			}
		} catch (IOException e) {
			CQRMain.logger.info("Failed to load protected region from file: " + file.getName(), e);
		}
	}

}
