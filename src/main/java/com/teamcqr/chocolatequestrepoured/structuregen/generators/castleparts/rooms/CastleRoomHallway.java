package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.decoration.RoomDecorTypes;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;

import net.minecraft.block.BlockGlazedTerracotta;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CastleRoomHallway extends CastleRoomGenericBase {
	public enum Alignment {
		VERTICAL, HORIZONTAL;

		private boolean canHaveInteriorWall(Direction side) {
			if (this == VERTICAL) {
				return (side == Direction.WEST || side == Direction.EAST);
			} else {
				return (side == Direction.NORTH || side == Direction.SOUTH);
			}
		}
	}

	private Alignment alignment;
	Direction patternStartFacing;

	public CastleRoomHallway(BlockPos startOffset, int sideLength, int height, Alignment alignment, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.HALLWAY;
		this.alignment = alignment;
		this.defaultFloor = true;
		this.defaultCeiling = true;
		this. patternStartFacing = Direction.HORIZONTALS[random.nextInt(Direction.HORIZONTALS.length)];

		this.decoSelector.registerMidDecor(RoomDecorTypes.NONE, 25);
		this.decoSelector.registerMidDecor(RoomDecorTypes.PILLAR, 1);

	}

	@Override
	protected void generateDefaultFloor(BlockStateGenArray genArray, DungeonCastle dungeon) {
		for (int z = 0; z < this.getDecorationLengthZ(); z++) {
			for (int x = 0; x < this.getDecorationLengthX(); x++) {
				BlockPos pos = this.getNonWallStartPos().add(x, 0, z);
				BlockState tcBlock = Blocks.GRAY_GLAZED_TERRACOTTA.getDefaultState();
				Direction tcFacing;

				//Terracotta patterns are formed in a 2x2 square from the pattern (going clockwise) N E S W
				//So create that pattern here given some starting facing
				if (pos.getZ() % 2 == 0) {
					if (pos.getX() % 2 == 0) {
						tcFacing = patternStartFacing;
					} else {
						tcFacing = patternStartFacing.rotateY();
					}
				} else {
					if (pos.getX() % 2 == 0) {
						tcFacing = patternStartFacing.rotateYCCW();
					} else {
						tcFacing = patternStartFacing.rotateY().rotateY();
					}
				}
				tcBlock = tcBlock.with(BlockGlazedTerracotta.FACING, tcFacing);
				genArray.addBlockState(pos, tcBlock, BlockStateGenArray.GenerationPhase.MAIN);
			}
		}
	}

	@Override
	protected BlockState getFloorBlock(DungeonCastle dungeon) {
		return Blocks.GRAY_GLAZED_TERRACOTTA.getDefaultState();
	}

	@Override
	public void addInnerWall(Direction side) {
		if (this.alignment.canHaveInteriorWall(side)) {
			super.addInnerWall(side);
		}
	}

	@Override
	public void copyPropertiesOf(CastleRoomBase room) {
		if (room instanceof CastleRoomHallway) {
			this.patternStartFacing = ((CastleRoomHallway) room).patternStartFacing;
		}
	}
}