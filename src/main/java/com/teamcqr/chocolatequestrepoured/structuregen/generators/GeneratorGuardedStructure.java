package com.teamcqr.chocolatequestrepoured.structuregen.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.teamcqr.chocolatequestrepoured.CQRMain;
import com.teamcqr.chocolatequestrepoured.structuregen.PlateauBuilder;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonBase;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonGuardedCastle;
import com.teamcqr.chocolatequestrepoured.structuregen.generation.ExtendedBlockStatePart;
import com.teamcqr.chocolatequestrepoured.structuregen.generation.IStructure;
import com.teamcqr.chocolatequestrepoured.structuregen.structurefile.CQStructure;
import com.teamcqr.chocolatequestrepoured.structuregen.structurefile.EPosType;
import com.teamcqr.chocolatequestrepoured.util.CQRConfig;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;
import com.teamcqr.chocolatequestrepoured.util.VectorUtil;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.template.PlacementSettings;

/**
 * Copyright (c) 29.04.2019
 * Developed by DerToaster98
 * GitHub: https://github.com/DerToaster98
 */
public class GeneratorGuardedStructure implements IDungeonGenerator {

	// DONE? remake the part where the dungeons are chosen and the support hills are being built, it does not work how it should atm...

	private DungeonGuardedCastle dungeon;

	private List<File> chosenStructures = new ArrayList<File>();
	private File centerStructure;

	private BlockPos startPos;
	private List<BlockPos> structurePosList = new ArrayList<BlockPos>();
	private List<Rotation> rotList = new ArrayList<Rotation>();

	private HashMap<CQStructure, BlockPos> toGenerate = new HashMap<CQStructure, BlockPos>();

	public GeneratorGuardedStructure(DungeonGuardedCastle dungeon) {
		this.dungeon = dungeon;
	}

	private World worldIn;

	@Override
	public void preProcess(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		// DONE: Calculate positions of structures, then build the support platforms, then calculate
		// !! IN BUILD STEP !! PATH BUILDING: First: Chose wether to build x or z first. then build x/z until the destination x/z is reached. then switch to the remaining component and wander to the destination
		BlockPos start = new BlockPos(x, y, z);
		this.startPos = new BlockPos(start);
		this.worldIn = world;
		int vX = DungeonGenUtils.getIntBetweenBorders(this.dungeon.getMinDistance(), this.dungeon.getMaxDistance());
		for (int i = 0; i < this.chosenStructures.size(); i++) {
			if (!this.dungeon.placeInCircle() && i > 0) {
				vX = DungeonGenUtils.getIntBetweenBorders(this.dungeon.getMinDistance(), this.dungeon.getMaxDistance());
			}
			Vec3i v = new Vec3i(vX, 0, 0);
			Double degrees = ((Integer) new Random().nextInt(360)).doubleValue();
			if (this.dungeon.placeInCircle()) {
				degrees = 360.0 / this.chosenStructures.size();
				degrees *= i;

				// System.out.println("Angle: " + degrees);
			}
			v = VectorUtil.rotateVectorAroundY(v, degrees);
			// System.out.println("Vector: " + v.toString());
			BlockPos newPos = start.add(v);
			while (this.positionConflicts(newPos) && i > 0 && !this.dungeon.placeInCircle()) {
				degrees = ((Integer) new Random().nextInt(360)).doubleValue();
				if (this.dungeon.placeInCircle()) {
					degrees = 360.0 / this.chosenStructures.size();
					degrees *= i;
				}
				v = VectorUtil.rotateVectorAroundY(v, degrees);

				newPos = start.add(v);
			}
			int yNew = DungeonGenUtils.getHighestYAt(world.getChunkAt(newPos), newPos.getX(), newPos.getZ(), true);

			BlockPos calculatedPos = new BlockPos(newPos.getX(), yNew, newPos.getZ());
			if (!this.structurePosList.contains(calculatedPos)) {
				this.structurePosList.add(calculatedPos);
			}
		}
	}

	private boolean positionConflicts(BlockPos newPos) {
		for (BlockPos pIn : this.structurePosList) {
			if (Math.sqrt(Math.abs(pIn.distanceSq(newPos.getX(), pIn.getY(), newPos.getZ(), false))) < (new Double((new Integer(this.dungeon.getMinDistance()).doubleValue() * 0.9D)))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void buildStructure(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		if (this.centerStructure == null) {
			CQRMain.logger.error("No center building for guarded castle: " + this.dungeon.getDungeonName());
			return;
		}
		CQStructure centerDun = new CQStructure(this.centerStructure);

		PlateauBuilder platformCenter = new PlateauBuilder();
		platformCenter.load(this.dungeon.getSupportBlock(), this.dungeon.getSupportTopBlock());
		lists.add(platformCenter.createSupportHillList(new Random(), world, new BlockPos(x, y + this.dungeon.getUnderGroundOffset(), z), centerDun.getSize().getX(), centerDun.getSize().getZ(), EPosType.DEFAULT));

		BlockPos cenPos = new BlockPos(x /*- (centerDun.getSizeX() /2)*/, y, z /*- (centerDun.getSizeZ() /2)*/);

		this.toGenerate.put(centerDun, cenPos);
		// this.shieldPos = centerDun.getShieldCorePosition();

		PlateauBuilder platform = new PlateauBuilder();
		platform.load(this.dungeon.getSupportBlock(), this.dungeon.getSupportTopBlock());
		// First, build all the support platforms
		for (int i = 0; i < this.structurePosList.size(); i++) {
			if (i < this.chosenStructures.size()) {
				// CQRMain.logger.info("Building support platform {}...", i + 1);
				// DONE: Load structures from file method !!HIGH PRIORITY!!
				CQStructure dungeonToSpawn = new CQStructure(this.chosenStructures.get(i));

				if (dungeonToSpawn != null) {
					// Build the support platform...
					BlockPos pos = this.structurePosList.get(i);

					int xT = pos.getX();
					int zT = pos.getZ();

					Rotation rot = this.dungeon.rotateDungeon() ? Rotation.values()[new Random().nextInt(4)] : Rotation.NONE;
					int sizeX = dungeonToSpawn.getSize().getX();
					int sizeZ = dungeonToSpawn.getSize().getZ();
					this.rotList.set(i, rot);
					if (this.dungeon.rotateDungeon()) {
						switch (rot) {
						case CLOCKWISE_90:
							xT -= sizeX;
							this.rotList.set(i, Rotation.CLOCKWISE_90);
							break;
						case CLOCKWISE_180:
							xT -= sizeX;
							zT -= sizeZ;
							this.rotList.set(i, Rotation.CLOCKWISE_180);
							break;
						case COUNTERCLOCKWISE_90:
							zT -= sizeZ;
							this.rotList.set(i, Rotation.COUNTERCLOCKWISE_90);
							break;
						default:
							break;
						}
						pos = new BlockPos(xT, pos.getY(), zT);
						this.structurePosList.set(i, pos);
					}

					lists.add(platform.createSupportHillList(new Random(), world, new BlockPos(pos.getX(), pos.getY() + this.dungeon.getUnderGroundOffset(), pos.getZ()), dungeonToSpawn.getSize().getX(), dungeonToSpawn.getSize().getZ(), EPosType.DEFAULT));

					// Build the structure...
					/*
					 * int Y = pos.getY() - this.dungeon.getUnderGroundOffset();
					 * int X = pos.getX() - (dungeonToSpawn.getSizeX() /2);
					 * int Z = pos.getZ() - (dungeonToSpawn.getSizeZ() /2);
					 * //pos = pos.add(- dungeonToSpawn.getSizeX() /2, 0, - dungeonToSpawn.getSizeZ() /2);
					 * pos = new BlockPos(X, Y, Z);
					 */

					this.toGenerate.put(dungeonToSpawn, pos);
				}
			}
		}
		// then build the paths...
		if (this.structurePosList != null && !this.structurePosList.isEmpty() && this.startPos != null && this.dungeon.buildPaths()) {
			// CQRMain.logger.info("Building {} roads...", this.structurePosList.size());
			for (BlockPos end : this.structurePosList) {
				// CQRMain.logger.info("Building road {} of {}...", this.structurePosList.indexOf(end) + 1, this.structurePosList.size());
				this.buildPath(end, cenPos /* this.startPos */);
			}
			// CQRMain.logger.info("Roads built!");
		}
		// And now, build all the structures...
	}

	@Override
	public void postProcess(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		if (this.toGenerate != null && !this.toGenerate.isEmpty()) {

			PlacementSettings plcmnt = new PlacementSettings();
			plcmnt.setMirror(Mirror.NONE);
			plcmnt.setRotation(Rotation.NONE);
			if (this.dungeon.rotateDungeon()) {
				plcmnt.setRotation(this.getRandomRotation());
			}
			//plcmnt.setIntegrity(1.0f);

			int index = 1;
			BlockPos posLower = new BlockPos(x, y, z);
			BlockPos posUpper = new BlockPos(x, y, z);
			//List<String> bosses = new ArrayList<>();
			for (CQStructure structure : this.toGenerate.keySet()) {
				// CQRMain.logger.info("Building house {}...", index);
				BlockPos pos = this.toGenerate.get(structure);
				int X = posLower.getX();
				int Y = posLower.getY();
				int Z = posLower.getZ();
				if (pos.getX() < X) {
					X = pos.getX();
				}
				if (pos.getY() < Y) {
					X = pos.getY();
				}
				if (pos.getZ() < Z) {
					X = pos.getZ();
				}
				posLower = new BlockPos(X, Y, Z);

				int xm = posUpper.getX();
				int ym = posUpper.getY();
				int zm = posUpper.getZ();
				if (pos.getX() + structure.getSize().getX() > xm) {
					xm = pos.getX() + structure.getSize().getX();
				}
				if (pos.getY() + structure.getSize().getY() > ym) {
					ym = pos.getY() + structure.getSize().getY();
				}
				if (pos.getZ() + structure.getSize().getZ() > zm) {
					zm = pos.getZ() + structure.getSize().getZ();
				}
				posUpper = new BlockPos(X, Y, Z);

				plcmnt.setRotation(this.rotList.get(index - 1));

				for (List<? extends IStructure> list : structure.addBlocksToWorld(world, pos, plcmnt, EPosType.DEFAULT, this.dungeon, chunk.getPos().x, chunk.getPos().z))
					lists.add(list);

				/*
				 * for (UUID id : structure.getBossIDs()) {
				 * bosses.add(id.toString());
				 * }
				 */

				index++;
			}

			/*
			 * CQDungeonStructureGenerateEvent event = new CQDungeonStructureGenerateEvent(this.dungeon, posLower, posUpper.subtract(posLower), world, bosses);
			 * event.setShieldCorePosition(shieldPos);
			 * MinecraftForge.EVENT_BUS.post(event);
			 */
		}
	}

	@Override
	public void fillChests(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {

	}

	@Override
	public void placeSpawners(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {

	}

	// Functionality: Things and methods for generating the paths / streets...
	public void addStructure(File f) {
		// if(!chosenStructures.contains(f)) {
		this.chosenStructures.add(f);
		// }
	}

	public void setCenterStructure(File f) {
		this.centerStructure = f;
	}

	private void buildPath(BlockPos start, BlockPos end) {
		boolean xfirst = new Random().nextBoolean();
		if (xfirst) {
			this.buildPathX(start, end);
			this.buildPathZ(start.add(end.getX() - start.getX(), 0, 0), end);
		} else {
			this.buildPathZ(start, end);
			this.buildPathX(start.add(0, 0, end.getZ() - start.getZ()), end);
		}
	}

	private void buildPathX(BlockPos start, BlockPos end) {
		Chunk currChunk = this.worldIn.getChunkAt(start);
		int vX = end.getX() < start.getX() ? -1 : 1;
		if (end.getX() == start.getX()) {
			vX = 0;
		}
		int currX = start.getX();
		int z = start.getZ();
		int y = 0;
		do {
			y = DungeonGenUtils.getHighestYAt(currChunk, currX, z, true);
			this.buildPathSegmentX(new BlockPos(currX, y, z));
			currX += vX;
			currChunk = this.worldIn.getChunkAt(new BlockPos(currX, y, z));
		} while (currX != end.getX());
		/*
		 * if(start.getZ() != end.getZ()) {
		 * start = new BlockPos(end.getX(), start.getY(), start.getZ());
		 * buildPathZ(start, end);
		 * }
		 */
	}

	private void buildPathZ(BlockPos start, BlockPos end) {
		Chunk currChunk = this.worldIn.getChunkAt(start);
		int vZ = end.getZ() < start.getZ() ? -1 : 1;
		if (end.getZ() == start.getZ()) {
			vZ = 0;
		}
		int currZ = start.getZ();
		int x = start.getX();
		int y = 0;
		do {
			y = DungeonGenUtils.getHighestYAt(currChunk, x, currZ, true);
			this.buildPathSegmentZ(new BlockPos(x, y, currZ));
			currZ += vZ;
			currChunk = this.worldIn.getChunkAt(new BlockPos(x, y, currZ));
		} while (currZ != end.getZ());
		/*
		 * if(start.getX() != end.getX()) {
		 * start = new BlockPos(start.getX(), start.getY(), end.getZ());
		 * buildPathZ(start, end);
		 * }
		 */
	}

	private void buildPathSegmentX(BlockPos pos) {
		this.worldIn.setBlockState(pos, this.dungeon.getPathMaterial().getDefaultState());
		this.worldIn.setBlockState(pos.north(), this.dungeon.getPathMaterial().getDefaultState());
		this.worldIn.setBlockState(pos.south(), this.dungeon.getPathMaterial().getDefaultState());

		this.supportBlock(pos);
		this.supportBlock(pos.north());
		this.supportBlock(pos.south());
	}

	private void buildPathSegmentZ(BlockPos pos) {
		this.worldIn.setBlockState(pos, this.dungeon.getPathMaterial().getDefaultState());
		this.worldIn.setBlockState(pos.west(), this.dungeon.getPathMaterial().getDefaultState());
		this.worldIn.setBlockState(pos.east(), this.dungeon.getPathMaterial().getDefaultState());

		this.supportBlock(pos);
		this.supportBlock(pos.west());
		this.supportBlock(pos.east());
	}

	private void supportBlock(BlockPos pos) {
		int i = 0;
		BlockPos tmpPos = pos.up();
		while (this.worldIn.getBlockState(tmpPos).getBlock() != Blocks.AIR && i <= 3) {
			this.worldIn.setBlockState(tmpPos, Blocks.AIR.getDefaultState());
			tmpPos = tmpPos.up();
		}
		tmpPos = pos.down();
		while (this.worldIn.getBlockState(tmpPos).getBlock() == Blocks.AIR) {
			this.worldIn.setBlockState(tmpPos, this.dungeon.getPathMaterial().getDefaultState());
			tmpPos = tmpPos.down();
		}
	}

	private Rotation getRandomRotation() {
		int index = new Random().nextInt(4);
		switch (index) {
		case 0:
			return Rotation.CLOCKWISE_90;
		case 1:
			return Rotation.CLOCKWISE_180;
		case 2:
			return Rotation.COUNTERCLOCKWISE_90;
		default:
			return Rotation.NONE;
		}
	}

	@Override
	public void placeCoverBlocks(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		if (this.dungeon.isCoverBlockEnabled()) {
			Map<BlockPos, ExtendedBlockStatePart.ExtendedBlockState> stateMap = new HashMap<>();
			for (CQStructure structure : this.toGenerate.keySet()) {
				int startX = this.toGenerate.get(structure).getX() - structure.getSize().getX() / 3 - CQRConfig.general.supportHillWallSize / 2;
				int startZ = this.toGenerate.get(structure).getZ() - structure.getSize().getZ() / 3 - CQRConfig.general.supportHillWallSize / 2;

				int endX = this.toGenerate.get(structure).getX() + structure.getSize().getX() + structure.getSize().getX() / 3 + CQRConfig.general.supportHillWallSize / 2;
				int endZ = this.toGenerate.get(structure).getZ() + structure.getSize().getZ() + structure.getSize().getZ() / 3 + CQRConfig.general.supportHillWallSize / 2;

				for (int iX = startX; iX <= endX; iX++) {
					for (int iZ = startZ; iZ <= endZ; iZ++) {
						BlockPos pos = new BlockPos(iX, world.getTopSolidOrLiquidBlock(new BlockPos(iX, 0, iZ)).getY(), iZ);
						if (world.getBlockState(pos.subtract(new Vec3i(0, 1, 0))).getBlock() != this.dungeon.getCoverBlock()) {
							stateMap.put(pos, new ExtendedBlockStatePart.ExtendedBlockState(this.dungeon.getCoverBlock().getDefaultState(), null));
						}
					}
				}
			}
			lists.add(ExtendedBlockStatePart.splitExtendedBlockStateMap(stateMap));
		}
	}

	@Override
	public DungeonBase getDungeon() {
		return this.dungeon;
	}

}
