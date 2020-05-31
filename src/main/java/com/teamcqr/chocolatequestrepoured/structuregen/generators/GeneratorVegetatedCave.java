package com.teamcqr.chocolatequestrepoured.structuregen.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.teamcqr.chocolatequestrepoured.structuregen.EDungeonMobType;
import com.teamcqr.chocolatequestrepoured.structuregen.WorldDungeonGenerator;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonBase;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonVegetatedCave;
import com.teamcqr.chocolatequestrepoured.structuregen.generation.ExtendedBlockStatePart;
import com.teamcqr.chocolatequestrepoured.structuregen.generation.ExtendedBlockStatePart.ExtendedBlockState;
import com.teamcqr.chocolatequestrepoured.structuregen.generation.IStructure;
import com.teamcqr.chocolatequestrepoured.structuregen.structurefile.CQStructure;
import com.teamcqr.chocolatequestrepoured.structuregen.structurefile.EPosType;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;
import com.teamcqr.chocolatequestrepoured.util.VectorUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.template.PlacementSettings;

public class GeneratorVegetatedCave implements IDungeonGenerator {

	private DungeonVegetatedCave dungeon;

	private List<BlockPos> spawners = new ArrayList<>();
	private List<BlockPos> chests = new ArrayList<>();
	private Set<BlockPos> ceilingBlocks = new HashSet<>();
	private Set<BlockPos> giantMushrooms = new HashSet<>();
	private Map<BlockPos, Integer> heightMap = new ConcurrentHashMap<>();
	private Set<BlockPos> floorBlocks = new HashSet<>();
	private Map<BlockPos, ExtendedBlockStatePart.ExtendedBlockState> blocks = new ConcurrentHashMap<>();
	private Block[][][] centralCaveBlocks;
	private CQStructure core = null;
	private EDungeonMobType mobtype;

	public GeneratorVegetatedCave(DungeonVegetatedCave dungeon) {
		this.dungeon = dungeon;
	}

	@Override
	public void preProcess(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		if (this.dungeon.getDungeonMob() == EDungeonMobType.DEFAULT) {
			dungeon.getDungeonMob();
			this.mobtype = EDungeonMobType.getMobTypeDependingOnDistance(world, x, z);
		} else {
			this.mobtype = dungeon.getDungeonMob();
		}
		Random random = new Random(WorldDungeonGenerator.getSeed(world, x / 16, z / 16));
		Block[][][] blocks = getRandomBlob(dungeon.getAirBlock(), dungeon.getCentralCaveSize(), random);
		this.centralCaveBlocks = blocks;
		if(dungeon.placeVines()) {
			this.ceilingBlocks.addAll(getCeilingBlocksOfBlob(blocks, new BlockPos(x,y,z), random));
		}
		this.floorBlocks.addAll(getFloorBlocksOfBlob(blocks, new BlockPos(x, y, z), random));
		storeBlockArrayInMap(blocks, new BlockPos(x, y, z));
		Vec3d center = new Vec3d(x, y - (dungeon.getCentralCaveSize() / 2), z);
		Vec3d rad = new Vec3d(dungeon.getCentralCaveSize() * 1.75, 0, 0);
		int tunnelCount = dungeon.getTunnelCount(random);
		double angle = 360D / tunnelCount;
		for (int i = 0; i < tunnelCount; i++) {
			Vec3d v = VectorUtil.rotateVectorAroundY(rad, angle * i);
			Vec3d startPos = center.add(v);
			createTunnel(startPos, angle * i, dungeon.getTunnelStartSize(), dungeon.getCaveSegmentCount(), random, lists);
		}
		// Filter floorblocks
		filterFloorBlocks();
		//Filter ceiling blocks
		if(dungeon.placeVines()) {
			filterCeilingBlocks(world);
		}

		// Flowers, Mushrooms and Weed
		if (this.dungeon.placeVegetation()) {
			createVegetation(random);
		}
		//Vines
		if(this.dungeon.placeVines()) {
			createVines(random);
		}

		// Build
		lists.add(ExtendedBlockStatePart.splitExtendedBlockStateMap(this.blocks));
	}

	@Override
	public void buildStructure(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		if (dungeon.placeBuilding()) {
			File file = dungeon.getRandomCentralBuilding();
			if (file != null) {
				CQStructure structure = new CQStructure(file);
				structure.setDungeonMob(this.mobtype);
				this.core = structure;
			}
		}
		// DONE: Paste the building
	}
	
	private int getLowestY(Block[][][] blocks, int rX, int rZ, int origY) {
		int y = 255;
		
		int cX = blocks.length /2;
		int radX = rX < cX ? rX : cX;
		if(cX + radX >= blocks.length) {
			radX = blocks.length - cX;
		}
		int cZ = blocks[0][0].length /2;
		int radZ = rZ < cZ ? rZ : cZ;
		if(cZ + radZ >= blocks.length) {
			radZ = blocks.length - cZ;
		}
		
		for(int iX = cX - radX; iX <= cX + radX; iX++) {
			for(int iZ = cZ - radZ; iZ <= cZ + radZ; iZ++) {
				if(iX < 0 || iX >= blocks.length || iZ < 0 || iZ >= blocks[0][0].length) {
					continue;
				}
				for(int iY = 0; iY < blocks[iX].length; iY++) {
					if(blocks[iX][iY][iZ] != null) {
						if(y > iY) {
							y = iY;
						}
						break;
					}
				}
			}
		}
		
		int radius = blocks.length / 2;
		y -= radius;
		y += origY;
		return y;
	}

	@Override
	public void postProcess(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		// DONE: Place giant shrooms
		Map<BlockPos, ExtendedBlockStatePart.ExtendedBlockState> stateMap = new HashMap<>();
		Random random = new Random(WorldDungeonGenerator.getSeed(world, x / 16, z / 16));
		for (BlockPos p : this.giantMushrooms) {
			// Place shroom
			if (random.nextBoolean()) {
				generateGiantMushroom(p, random, stateMap);
			}

			if (random.nextInt(3) == 0) {
				// Spawner
				BlockPos spawner = new BlockPos(p.getX() + (random.nextBoolean() ? -1 : 1), p.getY() + 1, p.getZ() + (random.nextBoolean() ? -1 : 1));
				this.spawners.add(spawner);
				if (random.nextInt(3) >= 1) {
					// Chest
					this.chests.add(spawner.down());
				}
			}
		}
		lists.add(ExtendedBlockStatePart.splitExtendedBlockStateMap(stateMap));
	}

	@Override
	public void fillChests(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		// DONE: Place and fill chests
		Map<BlockPos, ExtendedBlockStatePart.ExtendedBlockState> stateMap = new HashMap<>();
		Random random = new Random(WorldDungeonGenerator.getSeed(world, x / 16, z / 16));
		ResourceLocation[] chestIDs = this.dungeon.getChestIDs();
		for (BlockPos pos : this.chests) {
			Block block = Blocks.CHEST;
			BlockState state = block.getDefaultState();
			ChestTileEntity chest = (ChestTileEntity) block.createTileEntity(state, world);

			if (chest != null) {
				ResourceLocation resLoc = chestIDs[random.nextInt(chestIDs.length)];
				if (resLoc != null) {
					long seed = WorldDungeonGenerator.getSeed(world, x + pos.getX() + pos.getY(), z + pos.getZ() + pos.getY());
					chest.setLootTable(resLoc, seed);
				}
			}

			CompoundNBT nbt = chest.write(new CompoundNBT());
			stateMap.put(pos, new ExtendedBlockStatePart.ExtendedBlockState(state, nbt));
		}
		lists.add(ExtendedBlockStatePart.splitExtendedBlockStateMap(stateMap));
	}

	@Override
	public void placeSpawners(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		// DONE: Place spawners
		Map<BlockPos, ExtendedBlockStatePart.ExtendedBlockState> stateMap = new HashMap<>();
		for (BlockPos pos : this.spawners) {
			Block block = Blocks.SPAWNER;
			BlockState state = block.getDefaultState();
			MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) block.createTileEntity(state, world);
			spawner.getSpawnerBaseLogic().setEntityType(mobtype.getEntityResourceLocation());
			spawner.updateContainingBlockInfo();

			CompoundNBT nbt = spawner.write(new CompoundNBT());
			stateMap.put(pos, new ExtendedBlockStatePart.ExtendedBlockState(state, nbt));
		}
		lists.add(ExtendedBlockStatePart.splitExtendedBlockStateMap(stateMap));
	}

	@Override
	public void placeCoverBlocks(World world, Chunk chunk, int x, int y, int z, List<List<? extends IStructure>> lists) {
		//Well we need to place teh building now to avoid that it gets overrun by mushrooms
		if(this.core != null) {
			int pY = getLowestY(centralCaveBlocks, this.core.getSize().getX() /2, this.core.getSize().getZ() /2, y);
			BlockPos pastePos = new BlockPos(x, pY, z);
			// DONE: Support platform -> not needed
			PlacementSettings settings = new PlacementSettings();
			settings.setMirror(Mirror.NONE);
			settings.setRotation(Rotation.NONE);
			//settings.setReplacedBlock(Blocks.STRUCTURE_VOID);
			//settings.setIntegrity(1.0F);
			for (List<? extends IStructure> list : this.core.addBlocksToWorld(world, pastePos, settings, EPosType.CENTER_XZ_LAYER, this.dungeon, chunk.getPos().x, chunk.getPos().z)) {
				lists.add(list);
			}
		}
	}

	@Override
	public DungeonBase getDungeon() {
		return dungeon;
	}

	private void createTunnel(Vec3d startPos, double initAngle, int startSize, int initLength, Random random, List<List<? extends IStructure>> lists) {
		double angle = 90D;
		angle /= initLength;
		angle /= (startSize - 2) / 2;
		Vec3d expansionDir = VectorUtil.rotateVectorAroundY(new Vec3d(startSize, 0, 0), initAngle);
		for (int i = 0; i < initLength; i++) {
			Block[][][] blob = getRandomBlob(dungeon.getAirBlock(), startSize, (int) (startSize * 0.8), random);
			if(dungeon.placeVines()) {
				this.ceilingBlocks.addAll(getCeilingBlocksOfBlob(blob, new BlockPos(startPos.x, startPos.y, startPos.z), random));
			}
			this.floorBlocks.addAll(getFloorBlocksOfBlob(blob, new BlockPos(startPos.x, startPos.y, startPos.z), random));
			storeBlockArrayInMap(blob, new BlockPos(startPos.x, startPos.y, startPos.z));
			expansionDir = VectorUtil.rotateVectorAroundY(expansionDir, angle);
			startPos = startPos.add(expansionDir);
		}
		int szTmp = startSize;
		startSize -= 2;
		if (startSize > 3) {
			createTunnel(startPos, initAngle + angle * initLength - 90, new Integer(startSize), (int) (initLength * (szTmp / startSize)), random, lists);
			createTunnel(startPos, initAngle + angle * initLength, new Integer(startSize), (int) (initLength * (szTmp / startSize)), random, lists);
		}
	}

	private List<BlockPos> getCeilingBlocksOfBlob(Block[][][] blob, BlockPos blobCenter, Random random) {
		List<BlockPos> ceilingBlocks = new ArrayList<>();
		int radius = blob.length / 2;
		for (int iX = 0; iX < blob.length; iX++) {
			for (int iZ = 0; iZ < blob[0][0].length; iZ++) {
				for (int iY = blob[0].length -1; iY >= 1; iY--) {
					if (blob[iX][iY-1][iZ] != null && blob[iX][iY][iZ] == null) {
						//blob[iX][iY][iZ] = dungeon.getFloorBlock(random);
						BlockPos p = blobCenter.add(new BlockPos(iX - radius, iY - radius -1, iZ - radius));
						ceilingBlocks.add(p);
						int height = 0;
						int yTmp = iY -1;
						while(blob[iX][yTmp][iZ] != null && yTmp >= 0) {
							yTmp--;
							height++;
						}
						this.heightMap.put(p, new Integer(height));
						break;
					}
				}
			}
		}
		return ceilingBlocks;
	}

	private void storeBlockArrayInMap(Block[][][] blob, BlockPos blobCenter) {
		int radius = blob.length / 2;
		for (int iX = 0; iX < blob.length; iX++) {
			for (int iZ = 0; iZ < blob[0][0].length; iZ++) {
				for (int iY = 1; iY < blob[0].length; iY++) {
					if (blob[iX][iY][iZ] != null) {
						BlockState state = blob[iX][iY][iZ].getDefaultState();
						BlockPos bp = new BlockPos(iX - radius, iY - radius, iZ - radius);
						this.blocks.put(blobCenter.add(bp), new ExtendedBlockState(state, null));
					}
				}
			}
		}
	}

	private List<BlockPos> getFloorBlocksOfBlob(Block[][][] blob, BlockPos blobCenter, Random random) {
		List<BlockPos> floorBlocks = new ArrayList<>();
		int radius = blob.length / 2;
		for (int iX = 0; iX < blob.length; iX++) {
			for (int iZ = 0; iZ < blob[0][0].length; iZ++) {
				for (int iY = 1; iY < blob[0].length; iY++) {
					if (blob[iX][iY][iZ] != null && blob[iX][iY - 1][iZ] == null) {
						blob[iX][iY][iZ] = dungeon.getFloorBlock(random);
						floorBlocks.add(blobCenter.add(new BlockPos(iX - radius, iY - radius, iZ - radius)));
						break;
					}
				}
			}
		}
		return floorBlocks;
	}

	private Block[][][] getRandomBlob(Block block, int radius, Random random) {
		return getRandomBlob(block, radius, radius, random);
	}

	private Block[][][] getRandomBlob(Block block, int radius, int radiusY, Random random) {
		Block[][][] blocks = new Block[radius * 4][radiusY * 4][radius * 4];
		int subSphereCount = radius * 3;
		double sphereSurface = 4 * Math.PI * (radius * radius);
		double counter = sphereSurface / subSphereCount;
		double cI = 0;
		for (int iX = -radius; iX <= radius; iX++) {
			for (int iY = -radiusY; iY <= radiusY; iY++) {
				for (int iZ = -radius; iZ <= radius; iZ++) {
					double distance = iX * iX + iZ * iZ + iY * iY;
					distance = Math.sqrt(distance);
					if (distance < radius) {
						blocks[iX + (radius * 2)][iY + (radiusY * 2)][iZ + (radius * 2)] = block;
					} else if (distance <= radius + 1) {
						cI++;
						if (cI < counter) {
							continue;
						}
						cI = 0;
						int r1 = radius / 2;
						int r1Y = radiusY / 2;
						int r2 = (int) (radius * 0.75);
						int r2Y = (int) (radiusY * 0.75);
						int rSub = DungeonGenUtils.getIntBetweenBorders(r1, r2, random);
						int rSubY = DungeonGenUtils.getIntBetweenBorders(r1Y, r2Y, random);
						for (int jX = iX - rSub; jX <= iX + rSub; jX++) {
							for (int jY = iY - rSubY; jY <= iY + rSubY; jY++) {
								for (int jZ = iZ - rSub; jZ <= iZ + rSub; jZ++) {
									double distanceSub = (jX - iX) * (jX - iX) + (jY - iY) * (jY - iY) + (jZ - iZ) * (jZ - iZ);
									distanceSub = Math.sqrt(distanceSub);
									if (distanceSub < rSub) {
										try {
											if (blocks[jX + (radius * 2)][jY + (radiusY * 2)][jZ + (radius * 2)] != block) {
												blocks[jX + (radius * 2)][jY + (radiusY * 2)][jZ + (radius * 2)] = block;
											}
										} catch (ArrayIndexOutOfBoundsException ex) {
											// Ignore
										}
									}
								}
							}
						}
						subSphereCount--;
					}
				}
			}
		}
		return blocks;
	}

	private void filterFloorBlocks() {
		this.floorBlocks.removeIf(new Predicate<BlockPos>() {

			@Override
			public boolean test(BlockPos floorPos) {
				BlockPos lower = floorPos.down();
				if (blocks.containsKey(lower)) {
					blocks.put(floorPos, new ExtendedBlockState(dungeon.getAirBlock().getDefaultState(), null));
					return true;
				}
				return false;
			}
		});
	}
	
	private void filterCeilingBlocks(World world) {
		this.ceilingBlocks.removeIf(new Predicate<BlockPos>() {

			@Override
			public boolean test(BlockPos arg0) {
				BlockPos upper = arg0.up();
				if(blocks.containsKey(upper)) {
					blocks.put(arg0, new ExtendedBlockState(dungeon.getAirBlock().getDefaultState(), null));
					heightMap.remove(arg0);
					return true;
				}
				if(!dungeon.skipCeilingFiltering()) {
					return world.getHeight(Type.WORLD_SURFACE, arg0.getX(), arg0.getZ()) <= arg0.getY() || world.getHeight(Type.WORLD_SURFACE_WG, arg0).getY() <= arg0.getY() || world.canBlockSeeSky(arg0);
				}
				return false;
			}
			
		});
	}

	private void createVegetation(Random random) {
		for (BlockPos floorPos : this.floorBlocks) {
			int number = random.nextInt(300);
			BlockState state = null;
			if (number >= 295) {
				// Giant mushroom
				boolean flag = true;
				for (BlockPos shroom : giantMushrooms) {
					if (shroom.distanceSq(floorPos.getX(), floorPos.getY(), floorPos.getZ(), false) < 5 * 5) {
						flag = false;
						break;
					}
				}
				if(flag) {
					giantMushrooms.add(floorPos.up());
				}
			} else if (number >= 290) {
				// Lantern
				state = dungeon.getPumpkinBlock().getDefaultState();
			} else if (number <= 150) {
				if (number <= 100) {
					// Grass
					state = dungeon.getGrassBlock(random).getDefaultState();
				} else {
					// Flower or mushroom
					if (random.nextBoolean()) {
						// Flower
						state = dungeon.getFlowerBlock(random).getDefaultState();
					} else {
						// Mushroom
						state = dungeon.getMushroomBlock(random).getDefaultState();
					}
				}
			}
			if (state != null) {
				blocks.put(floorPos.up(), new ExtendedBlockState(state, null));
			}
		}
		//System.out.println("Floor blocks: " + floorBlocks.size());
		//System.out.println("Giant mushrooms: " + giantMushrooms.size());
	}
	
	private void createVines(Random random) {
		for(BlockPos vineStart : this.ceilingBlocks) {
			if(random.nextInt(300) >= (300 - dungeon.getVineChance())) {
				int vineLength = this.heightMap.get(vineStart);
				vineLength = new Double(vineLength / this.dungeon.getVineLengthModifier()).intValue();
				BlockPos vN = vineStart.north();
				BlockPos vE = vineStart.east();
				BlockPos vS = vineStart.south();
				BlockPos vW = vineStart.west();
				if(this.dungeon.isVineShapeCross()) {
					this.blocks.put(vineStart, new ExtendedBlockState(this.dungeon.getVineLatchBlock().getDefaultState(), null));
				}
				ExtendedBlockState airState = new ExtendedBlockState(dungeon.getAirBlock().getDefaultState(), null);
				ExtendedBlockState sState = dungeon.isVineShapeCross() ? new ExtendedBlockState(dungeon.getVineBlock().getDefaultState().with(VineBlock.NORTH, true), null) : null;
				ExtendedBlockState wState = dungeon.isVineShapeCross() ? new ExtendedBlockState(dungeon.getVineBlock().getDefaultState().with(VineBlock.EAST, true), null) : null;
				ExtendedBlockState nState = dungeon.isVineShapeCross() ? new ExtendedBlockState(dungeon.getVineBlock().getDefaultState().with(VineBlock.SOUTH, true), null) : null;
				ExtendedBlockState eState = dungeon.isVineShapeCross() ? new ExtendedBlockState(dungeon.getVineBlock().getDefaultState().with(VineBlock.WEST, true), null) : null;
				while(vineLength >= 0) {
					if(this.dungeon.isVineShapeCross()) {
						this.blocks.put(vN, nState);
						this.blocks.put(vE, eState);
						this.blocks.put(vS, sState);
						this.blocks.put(vW, wState);
						vN = vN.down();
						vE = vE.down();
						vS = vS.down();
						vW = vW.down();
						if(this.blocks.getOrDefault(vN, airState).getState().getBlock() != this.dungeon.getAirBlock() ||
								this.blocks.getOrDefault(vE, airState).getState().getBlock() != this.dungeon.getAirBlock() ||
								this.blocks.getOrDefault(vS, airState).getState().getBlock() != this.dungeon.getAirBlock() ||
								this.blocks.getOrDefault(vW, airState).getState().getBlock() != this.dungeon.getAirBlock()
							) 
						{
							break;
						}
					} else {
						this.blocks.put(vineStart, new ExtendedBlockState(this.dungeon.getVineBlock().getDefaultState(), null));
						if(this.blocks.getOrDefault(vineStart, airState).getState().getBlock() != this.dungeon.getAirBlock()) {
							break;
						}
						vineStart = vineStart.down();
					}
					vineLength--;
				}
			}
		}
	}

	private void generateGiantMushroom(BlockPos position, Random rand, Map<BlockPos, ExtendedBlockState> stateMap) {
		//Taken from WorldGenBigMushroom
		Block block = rand.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
		int i = 6;

		if (position.getY() >= 1 && position.getY() + i + 1 < 256) {

			int k2 = position.getY() + i;

			if (block == Blocks.RED_MUSHROOM_BLOCK) {
				k2 = position.getY() + i - 3;
			}

			for (int l2 = k2; l2 <= position.getY() + i; ++l2) {
				int j3 = 1;

				if (l2 < position.getY() + i) {
					++j3;
				}

				if (block == Blocks.BROWN_MUSHROOM_BLOCK) {
					j3 = 3;
				}

				int k3 = position.getX() - j3;
				int l3 = position.getX() + j3;
				int j1 = position.getZ() - j3;
				int k1 = position.getZ() + j3;

				for (int l1 = k3; l1 <= l3; ++l1) {
					for (int i2 = j1; i2 <= k1; ++i2) {
						int j2 = 5;

						if (l1 == k3) {
							--j2;
						} else if (l1 == l3) {
							++j2;
						}

						if (i2 == j1) {
							j2 -= 3;
						} else if (i2 == k1) {
							j2 += 3;
						}

						HugeMushroomBlock.EnumType blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.byMetadata(j2);

						if (block == Blocks.BROWN_MUSHROOM_BLOCK || l2 < position.getY() + i) {
							if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
								continue;
							}

							if (l1 == position.getX() - (j3 - 1) && i2 == j1) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.NORTH_WEST;
							}

							if (l1 == k3 && i2 == position.getZ() - (j3 - 1)) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.NORTH_WEST;
							}

							if (l1 == position.getX() + (j3 - 1) && i2 == j1) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.NORTH_EAST;
							}

							if (l1 == l3 && i2 == position.getZ() - (j3 - 1)) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.NORTH_EAST;
							}

							if (l1 == position.getX() - (j3 - 1) && i2 == k1) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.SOUTH_WEST;
							}

							if (l1 == k3 && i2 == position.getZ() + (j3 - 1)) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.SOUTH_WEST;
							}

							if (l1 == position.getX() + (j3 - 1) && i2 == k1) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.SOUTH_EAST;
							}

							if (l1 == l3 && i2 == position.getZ() + (j3 - 1)) {
								blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.SOUTH_EAST;
							}
						}

						if (blockhugemushroom$enumtype == HugeMushroomBlock.EnumType.CENTER && l2 < position.getY() + i) {
							blockhugemushroom$enumtype = HugeMushroomBlock.EnumType.ALL_INSIDE;
						}

						if (position.getY() >= position.getY() + i - 1 || blockhugemushroom$enumtype != HugeMushroomBlock.EnumType.ALL_INSIDE) {
							BlockPos blockpos = new BlockPos(l1, l2, i2);
							//BlockState state = worldIn.getBlockState(blockpos);

							// PUT IN MAP
							//this.setBlockAndNotifyAdequately(worldIn, blockpos, block.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, blockhugemushroom$enumtype));
							stateMap.put(blockpos, new ExtendedBlockState(block.getDefaultState().withProperty(HugeMushroomBlock.VARIANT, blockhugemushroom$enumtype), null));
						}
					}
				}
			}

			for (int i3 = 0; i3 < i; ++i3) {
				//BlockState iblockstate = worldIn.getBlockState(position.up(i3));
				// PUT IN MAP
				//this.setBlockAndNotifyAdequately(worldIn, position.up(i3), block.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM));
				stateMap.put(position.up(i3), new ExtendedBlockState(block.getDefaultState().withProperty(HugeMushroomBlock.VARIANT, HugeMushroomBlock.EnumType.STEM), null));
			}

		}
	}

}
