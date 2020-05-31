package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class CastleRoomBossStairMain extends CastleRoomDecoratedBase {
	private Direction doorSide;
	private int numRotations;
	private static final int ROOMS_LONG = 2;
	private static final int ROOMS_SHORT = 1;
	private static final int TOP_LANDING_BUFFER_Z = 3;
	private static final int MAIN_LANDING_Z = 2;
	private static final int MAIN_LANDING_X = 7;
	private static final int UPPER_STAIR_X = 3;
	private static final int LOWER_LANDING_Z = 2;
	private static final int LOWER_STAIRS_Z = 2;
	private static final int LOWER_STAIRS_LEN = 2;
	private static final int FLOOR_HEIGHT = 1;
	private static final int MID_STAIR_LENGTH = 2;

	private int endX;
	private int lenX;
	private int endZ;
	private int lenZ;
	private int maxHeightIdx;
	private int topStairLength;

	private int mainLandingXStartIdx;
	private int mainLandingXEndIdx;
	private int mainLandingZStartIdx;

	private int upperStairXStartIdx;
	private int upperStairXEndIdx;

	private int lowerStair1XStartIdx;
	private int lowerStair1XEndIdx;
	private int lowerStair2XStartIdx;
	private int lowerStair2XEndIdx;

	private int lowerLanding1XStartIdx;
	private int lowerLanding1XEndIdx;
	private int lowerLanding2XStartIdx;
	private int lowerLanding2XEndIdx;
	private int lowerLandingZStartIdx;
	private int midStairsZStartIdx;
	private int mainLandingMaxHeightIdx;
	private int lowerLandingMaxHeightIdx;

	public CastleRoomBossStairMain(BlockPos startOffset, int sideLength, int height, Direction doorSide, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.STAIRCASE_BOSS;

		this.doorSide = doorSide;
		this.numRotations = DungeonGenUtils.getCWRotationsBetween(Direction.NORTH, this.doorSide);

		this.endX = ROOMS_LONG * sideLength - 2; // minus 1 for the wall and 1 so it's at the last index
		this.lenX = this.endX + 1;
		this.endZ = ROOMS_SHORT * sideLength - 2; // minus 1 for the wall and 1 so it's at the last index
		this.lenZ = this.endZ + 1;
		this.maxHeightIdx = height - 1;

		this.topStairLength = this.lenZ - TOP_LANDING_BUFFER_Z - MAIN_LANDING_Z;
		final int lowerStairLength = height - FLOOR_HEIGHT - MID_STAIR_LENGTH - this.topStairLength;

		this.mainLandingXStartIdx = sideLength - 4;
		this.mainLandingXEndIdx = this.mainLandingXStartIdx + MAIN_LANDING_X - 1;
		this.mainLandingZStartIdx = this.endZ - MAIN_LANDING_Z + 1;

		this.upperStairXStartIdx = sideLength - 2;
		this.upperStairXEndIdx = this.upperStairXStartIdx + UPPER_STAIR_X - 1;

		this.lowerLanding1XStartIdx = this.upperStairXStartIdx - 2;
		this.lowerLanding1XEndIdx = this.lowerLanding1XStartIdx + 1;
		this.lowerLanding2XStartIdx = this.upperStairXEndIdx + 1;
		this.lowerLanding2XEndIdx = this.lowerLanding2XStartIdx + 1;

		this.lowerStair1XStartIdx = this.lowerLanding1XStartIdx - lowerStairLength;
		this.lowerStair1XEndIdx = this.lowerStair1XStartIdx + lowerStairLength - 1;
		this.lowerStair2XStartIdx = this.lowerLanding2XEndIdx + 1;
		this.lowerStair2XEndIdx = this.lowerStair2XStartIdx + lowerStairLength - 1;

		this.midStairsZStartIdx = this.mainLandingZStartIdx - LOWER_STAIRS_Z;
		this.lowerLandingZStartIdx = this.midStairsZStartIdx - LOWER_LANDING_Z;

		this.mainLandingMaxHeightIdx = height - this.topStairLength - 1;
		this.lowerLandingMaxHeightIdx = this.mainLandingMaxHeightIdx - LOWER_STAIRS_LEN;
	}

	@Override
	public void generateRoom(BlockStateGenArray genArray, DungeonCastle dungeon) {
		Vec3i offset;

		for (int x = 0; x <= this.endX; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z <= this.endZ; z++) {
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

		if (y == 0) {
			blockToBuild = this.getFloorBlock(dungeon);
		} else if (y == this.maxHeightIdx) {
			if (x >= this.upperStairXStartIdx && x <= this.upperStairXEndIdx) {
				if (z == TOP_LANDING_BUFFER_Z) {
					Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.NORTH, this.numRotations);
					return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, stairFacing);
				} else if (z < TOP_LANDING_BUFFER_Z) {
					return dungeon.getMainBlockState();
				}
			} else {
				blockToBuild = dungeon.getMainBlockState();
			}
		} else if ((x >= this.mainLandingXStartIdx && x <= this.mainLandingXEndIdx) && (z >= this.mainLandingZStartIdx)) {
			blockToBuild = this.getMainLandingBlock(x, y, z);
		} else if ((x >= this.upperStairXStartIdx && x <= this.upperStairXEndIdx) && ((z >= TOP_LANDING_BUFFER_Z) && (z <= TOP_LANDING_BUFFER_Z + this.topStairLength - 1))) {
			blockToBuild = this.getUpperStairBlock(x, y, z);
		} else if ((x >= this.lowerLanding1XStartIdx && x <= this.lowerLanding1XEndIdx) || (x >= this.lowerLanding2XStartIdx && x <= this.lowerLanding2XEndIdx)) {
			if (z == this.midStairsZStartIdx || z == this.midStairsZStartIdx + 1) {
				blockToBuild = this.getMidStairBlock(x, y, z);
			} else if (z == this.lowerLandingZStartIdx || z == this.lowerLandingZStartIdx + 1) {
				blockToBuild = this.getLowerLandingBlock(x, y, z);
			}
		} else if ((x >= this.lowerStair1XStartIdx && x <= this.lowerStair1XEndIdx) && (z == this.lowerLandingZStartIdx || z == this.lowerLandingZStartIdx + 1)) {
			blockToBuild = this.getLowerStair1Block(x, y, z);
		} else if ((x >= this.lowerStair2XStartIdx && x <= this.lowerStair2XEndIdx) && (z == this.lowerLandingZStartIdx || z == this.lowerLandingZStartIdx + 1)) {
			blockToBuild = this.getLowerStair2Block(x, y, z);
		}

		return blockToBuild;
	}

	private BlockState getLowerStair1Block(int x, int y, int z) {
		if (y == this.lowerLandingMaxHeightIdx - (this.lowerStair1XEndIdx - x)) {
			Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.EAST, this.numRotations);
			return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, stairFacing);
		} else if (y <= this.lowerLandingMaxHeightIdx - (this.lowerLanding1XEndIdx - x)) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	private BlockState getLowerStair2Block(int x, int y, int z) {
		if (y == this.lowerLandingMaxHeightIdx - (x - this.lowerStair2XStartIdx)) {
			Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.WEST, this.numRotations);
			return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, stairFacing);
		} else if (y <= this.lowerLandingMaxHeightIdx - (x - this.lowerStair2XStartIdx)) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	private BlockState getLowerLandingBlock(int x, int y, int z) {
		if (y >= 1 && y <= this.lowerLandingMaxHeightIdx) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	private BlockState getMidStairBlock(int x, int y, int z) {
		if (y == this.mainLandingMaxHeightIdx - (this.endZ - z - MAIN_LANDING_Z)) {
			Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.SOUTH, this.numRotations);
			return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, stairFacing);
		} else if (y < this.mainLandingMaxHeightIdx - (this.endZ - z - MAIN_LANDING_Z)) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	private BlockState getUpperStairBlock(int x, int y, int z) {
		if (y == (this.maxHeightIdx - (z - TOP_LANDING_BUFFER_Z))) {
			Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.NORTH, this.numRotations);
			return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, stairFacing);
		} else if ((y < this.maxHeightIdx - (z - TOP_LANDING_BUFFER_Z))) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	public BlockState getMainLandingBlock(int x, int y, int z) {
		if (y >= 1 && y <= this.mainLandingMaxHeightIdx) {
			return Blocks.STONE_BRICKS.getDefaultState();
		} else {
			return Blocks.AIR.getDefaultState();
		}
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
		if (!(this.doorSide.getAxis() == Direction.Axis.X && side == Direction.SOUTH) && !(this.doorSide.getAxis() == Direction.Axis.Z && side == Direction.EAST)) {
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
