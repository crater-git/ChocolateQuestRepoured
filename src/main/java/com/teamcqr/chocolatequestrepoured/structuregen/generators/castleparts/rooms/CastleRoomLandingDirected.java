package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CastleRoomLandingDirected extends CastleRoomBase {
	protected int openingWidth;
	protected int openingSeparation;
	protected int stairZ;
	protected Direction stairStartSide;

	public CastleRoomLandingDirected(BlockPos startOffset, int sideLength, int height, CastleRoomStaircaseDirected stairsBelow, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.LANDING_DIRECTED;
		this.openingWidth = stairsBelow.getUpperStairWidth();
		this.stairZ = stairsBelow.getUpperStairEndZ() + 1;
		this.openingSeparation = stairsBelow.getCenterStairWidth();
		this.stairStartSide = stairsBelow.getDoorSide();
		this.defaultCeiling = true;
	}

	@Override
	public void generateRoom(BlockStateGenArray genArray, DungeonCastle dungeon) {
		BlockState blockToBuild;

		//If stairs are facing to the east or west, need to flip the build lengths since we are essentially
		//generating a room facing south and then rotating it
		int lenX = this.stairStartSide.getAxis() == Direction.Axis.Z ? this.buildLengthX : this.buildLengthZ;
		int lenZ = this.stairStartSide.getAxis() == Direction.Axis.Z ? this.buildLengthZ : this.buildLengthX;

		for (int x = 0; x < lenX - 1; x++) {
			for (int z = 0; z < lenZ - 1; z++) {
				for (int y = 0; y < this.height - 1; y++) {
					blockToBuild = Blocks.AIR.getDefaultState();
					if (y == 0) {
						if (z > this.stairZ) {
							blockToBuild = dungeon.getFloorBlockState();
						} else if (x < this.openingWidth || ((x >= this.openingSeparation + this.openingWidth) && (x < this.openingSeparation + this.openingWidth * 2))) {
							if (z == this.stairZ) {
								Direction stairFacing = DungeonGenUtils.rotateFacingNTimesAboutY(Direction.SOUTH, DungeonGenUtils.getCWRotationsBetween(Direction.SOUTH, this.stairStartSide));
								blockToBuild = dungeon.getWoodStairBlockState().with(StairsBlock.FACING, stairFacing);
							}
						} else {
							blockToBuild = dungeon.getFloorBlockState();
						}
					}

					genArray.addBlockState(this.getRotatedPlacement(x, y, z, this.stairStartSide), blockToBuild, BlockStateGenArray.GenerationPhase.MAIN);
				}
			}
		}
	}

	@Override
	public boolean canBuildDoorOnSide(Direction side) {
		// Really only works on this side, could add logic to align the doors for other sides later
		return (side == this.stairStartSide);
	}

	@Override
	public boolean reachableFromSide(Direction side) {
		return (side == this.stairStartSide || side == this.stairStartSide.getOpposite());
	}
}
