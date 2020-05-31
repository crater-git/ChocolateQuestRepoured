package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.teamcqr.chocolatequestrepoured.objects.factories.GearedMobFactory;
import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.DungeonCastle;
import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;
import com.teamcqr.chocolatequestrepoured.util.GenerationTemplate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CastleRoomNetherPortal extends CastleRoomDecoratedBase
{
    private enum Alignment {
        HORIZONTAL,
        VERTICAL
    }

    private Alignment portalAlignment;

    public CastleRoomNetherPortal(BlockPos startOffset, int sideLength, int height, int floor) {
        super(startOffset, sideLength, height, floor);
        this.roomType = EnumRoomType.PORTAL;
        this.maxSlotsUsed = 1;
        this.defaultCeiling = true;
        this.defaultFloor = true;
        this.portalAlignment = random.nextBoolean() ? Alignment.HORIZONTAL : Alignment.VERTICAL;
    }

    @Override
    protected void generateRoom(BlockStateGenArray genArray, DungeonCastle dungeon) {
        int endX = getDecorationLengthX() - 1;
        int endZ = getDecorationLengthZ() - 1;
        int halfX = endX / 2;
        int halfZ = endZ / 2;

        int xStart = halfX - 2;
        int xEnd = halfX + 3;
        int zStart = halfZ - 2;
        int zEnd = halfZ + 2;

        Predicate<Vec3i> firstLayer = (v -> (v.getY() == 0));
        Predicate<Vec3i> northEdge = firstLayer.and(v -> (v.getX() >= xStart) && (v.getX() <= xEnd) && (v.getZ() == zStart));
        Predicate<Vec3i> southEdge = firstLayer.and(v -> (v.getX() >= xStart) && (v.getX() <= xEnd) && (v.getZ() == zEnd));
        Predicate<Vec3i> westEdge = firstLayer.and(v -> (v.getZ() >= zStart) && (v.getZ() <= zEnd) && (v.getX() == xStart));
        Predicate<Vec3i> eastEdge = firstLayer.and(v -> (v.getZ() >= zStart) && (v.getZ() <= zEnd) && (v.getX() == xEnd));
        Predicate<Vec3i> portalBot = (v -> (v.getY() == 0) && (v.getZ() == halfZ) && (v.getX() >= xStart + 1) && (v.getX() <= xEnd - 1));
        Predicate<Vec3i> portalTop = (v -> (v.getY() == 4) && (v.getZ() == halfZ) && (v.getX() >= xStart + 1) && (v.getX() <= xEnd - 1));
        Predicate<Vec3i> portalSides = (v -> (v.getY() > 0) && (v.getY() < 4) && (v.getZ() == halfZ) && ((v.getX() == xStart + 1) || (v.getX() == xEnd - 1)));
        Predicate<Vec3i> portalMid = (v -> (v.getY() > 0) && (v.getY() < 4) && (v.getZ() == halfZ) && ((v.getX() > xStart + 1) && (v.getX() < xEnd - 1)));
        Predicate<Vec3i> portal = portalBot.or(portalTop).or(portalSides);
        Predicate<Vec3i> platform = portal.negate().and(firstLayer.and(v-> (v.getX() >= xStart + 1) && (v.getX() <= xEnd - 1) && (v.getZ() >= zStart + 1) && (v.getZ() <= zEnd - 1)));


        GenerationTemplate portalRoomTemplate = new GenerationTemplate(getDecorationLengthX(), getDecorationLengthY(), getDecorationLengthZ());
        portalRoomTemplate.addRule(northEdge, dungeon.getWoodStairBlockState().with(StairsBlock.FACING, Direction.SOUTH));
        portalRoomTemplate.addRule(southEdge, dungeon.getWoodStairBlockState().with(StairsBlock.FACING, Direction.NORTH));
        portalRoomTemplate.addRule(westEdge, dungeon.getWoodStairBlockState().with(StairsBlock.FACING, Direction.EAST));
        portalRoomTemplate.addRule(eastEdge, dungeon.getWoodStairBlockState().with(StairsBlock.FACING, Direction.WEST));
        portalRoomTemplate.addRule(platform, dungeon.getMainBlockState());
        portalRoomTemplate.addRule(portal, Blocks.OBSIDIAN.getDefaultState());
        portalRoomTemplate.addRule(portalMid, Blocks.NETHER_PORTAL.getDefaultState());


        HashMap<BlockPos, BlockState> genMap = portalRoomTemplate.GetGenerationMap(getDecorationStartPos(), true);
        genArray.addBlockStateMap(genMap, BlockStateGenArray.GenerationPhase.MAIN);
        for (Map.Entry<BlockPos, BlockState> entry : genMap.entrySet()) {
            if (entry.getValue().getBlock() != Blocks.AIR) {
                usedDecoPositions.add(entry.getKey());
            }
        }

    }

    @Override
    protected BlockState getFloorBlock(DungeonCastle dungeon) {
        return dungeon.getMainBlockState();
    }

    @Override
    public void decorate(World world, BlockStateGenArray genArray, DungeonCastle dungeon, GearedMobFactory mobFactory)
    {
        setupDecoration(genArray);
        addWallDecoration(world, genArray, dungeon);
        addSpawners(world, genArray, dungeon, mobFactory);
        fillEmptySpaceWithAir(genArray);
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
}
