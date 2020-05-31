package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.segments.RoomWallBuilder;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.segments.WalkableRoofWallBuilder;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;

import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CastleRoomWalkableRoof extends CastleRoomBase {
	public CastleRoomWalkableRoof(BlockPos startOffset, int sideLength, int height, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.WALKABLE_ROOF;
		this.pathable = false;
	}

	@Override
	public void generateRoom(BlockStateGenArray genArray, DungeonCastle dungeon) {
		;
	}

	@Override
	public void postProcess(BlockStateGenArray genArray, DungeonCastle dungeon)
	{
		for (BlockPos pos : this.getDecorationArea()) {
			genArray.addBlockState(pos, Blocks.AIR.getDefaultState(), BlockStateGenArray.GenerationPhase.MAIN);
		}
	}

	@Override
	public void addInnerWall(Direction side) {
		; // Do nothing because walkable roofs don't need inner walls
	}

	@Override
	protected void createAndGenerateWallBuilder(BlockStateGenArray genArray, DungeonCastle dungeon, Direction side, int wallLength, BlockPos wallStart) {
		RoomWallBuilder builder = new WalkableRoofWallBuilder(wallStart, this.height, wallLength, this.walls.getOptionsForSide(side), side);
		builder.generate(genArray, dungeon);
	}

	@Override
	protected boolean hasFloor()
	{
		return false;
	}
}
