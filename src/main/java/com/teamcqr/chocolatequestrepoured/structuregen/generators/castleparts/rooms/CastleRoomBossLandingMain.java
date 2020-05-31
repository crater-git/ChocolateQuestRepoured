package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.objects.factories.GearedMobFactory;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CastleRoomBossLandingMain extends CastleRoomDecoratedBase {
	private static final int ROOMS_LONG = 2;
	private static final int ROOMS_SHORT = 1;
	private static final int LANDING_LENGTH_X = 3;
	private static final int LANDING_LENGTH_Z = 2;
	private static final int STAIR_OPENING_LENGTH_Z = 2;
	private static final int BOSS_ROOM_WIDTH = 17; // may want to get this from the boss room itself later

	private Direction doorSide;
	private int numRotations;

	private int endX;
	private int lenX;
	private int endZ;
	private int lenZ;

	private int connectingWallLength;

	private int stairOpeningXStartIdx;
	private int stairOpeningXEndIdx;
	private int stairsDownZIdx;
	private int stairOpeningZStartIdx;
	private int stairOpeningZEndIdx;

	public CastleRoomBossLandingMain(BlockPos startOffset, int sideLength, int height, Direction doorSide, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.LANDING_BOSS;
		this.doorSide = doorSide;
		this.numRotations = DungeonGenUtils.getCWRotationsBetween(Direction.NORTH, this.doorSide);
		this.defaultCeiling = true;

		this.endX = ROOMS_LONG * sideLength - 2; // minus 1 for the wall and 1 so it's at the last index
		this.lenX = this.endX + 1;
		this.endZ = ROOMS_SHORT * sideLength - 2; // minus 1 for the wall and 1 so it's at the last index
		this.lenZ = this.endZ + 1;

		this.stairOpeningXStartIdx = sideLength - 2;
		this.stairOpeningXEndIdx = this.stairOpeningXStartIdx + LANDING_LENGTH_X - 1;

		this.stairsDownZIdx = LANDING_LENGTH_Z;
		this.stairOpeningZStartIdx = LANDING_LENGTH_Z + 1;
		this.stairOpeningZEndIdx = this.stairOpeningZStartIdx + STAIR_OPENING_LENGTH_Z;

		final int gapToBossRoom = 2 + BOSS_ROOM_WIDTH - this.lenX;
		this.connectingWallLength = 0;
		if (gapToBossRoom > 0) {
			// Determine size of walls that come in from each side so there is no space between this and boss room
			this.connectingWallLength = (int) Math.ceil((double) (gapToBossRoom) / 2);
		}
	}

	@Override
	public void generateRoom(BlockStateGenArray genArray, DungeonCastle dungeon) {
		Vec3i offset;

		for (int x = 0; x <= this.endX; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = -1; z <= this.endZ; z++) {
					BlockState blockToBuild = this.getBlockToBuild(dungeon, x, y, z);

					offset = DungeonGenUtils.rotateMatrixOffsetCW(new Vec3i(x, y, z), this.lenX, this.lenZ, this.numRotations);
					genArray.addBlockState(this.origin.add(offset), blockToBuild, BlockStateGenArray.GenerationPhase.MAIN);

					if (blockToBuild.getBlock() != Blocks.AIR) {
						this.usedDecoPositions.add(this.origin.add(offset));
					}
				}
			}
		}
	}

	private BlockState getBlockToBuild(DungeonCastle dungeon, int x, int y, int z) {
		BlockState blockToBuild = Blocks.AIR.getDefaultState();

		if (z == -1) {
			if (x < this.connectingWallLength || x > this.endX - this.connectingWallLength || y == this.height - 1) {
				blockToBuild = dungeon.getMainBlockState();
			} else if (y == 0) {
				return Blocks.QUARTZ_BLOCK.getDefaultState();
			}
		} else if (y == 0) {
			if ((x >= this.stairOpeningXStartIdx) && (x <= this.stairOpeningXEndIdx)) {
				if (z < this.stairsDownZIdx) {
					blockToBuild = Blocks.QUARTZ_BLOCK.getDefaultState();
				} else if (z == this.stairsDownZIdx) {
					Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.NORTH, this.numRotations);
					blockToBuild = dungeon.getWoodStairBlockState().with(StairsBlock.FACING, stairFacing);
				} else {
					return Blocks.AIR.getDefaultState();
				}
			} else {
				blockToBuild = Blocks.QUARTZ_BLOCK.getDefaultState();
			}
		} else if (y == this.height - 1) {
			blockToBuild = dungeon.getMainBlockState();
		}

		return blockToBuild;
	}

	@Override
	public void decorate(World world, BlockStateGenArray genArray, DungeonCastle dungeon, GearedMobFactory mobFactory) {
		this.addEdgeDecoration(world, genArray, dungeon);
		this.addWallDecoration(world, genArray, dungeon);
		this.addSpawners(world, genArray, dungeon, mobFactory);
	}

	@Override
	boolean shouldBuildEdgeDecoration() {
		return false;
	}

	@Override
	boolean shouldBuildWallDecoration() {
		return true;
	}

	@Override
	boolean shouldBuildMidDecoration() {
		return false;
	}

	@Override
	boolean shouldAddSpawners() {
		return true;
	}

	@Override
	boolean shouldAddChests() {
		return false;
	}

	@Override
	public void addInnerWall(Direction side) {
		if (!(this.doorSide.getAxis() == Direction.Axis.X && side == Direction.SOUTH) && !(this.doorSide.getAxis() == Direction.Axis.Z && side == Direction.EAST) && !(side == this.doorSide)) {
			super.addInnerWall(side);
		}
	}

	@Override
	public boolean canBuildDoorOnSide(Direction side) {
		return true;
	}

	@Override
	public boolean reachableFromSide(Direction side) {
		return true;
	}
}
