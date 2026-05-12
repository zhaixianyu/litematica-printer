package me.aleksilassila.litematica.printer.printer.bedrockUtils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;

import static me.aleksilassila.litematica.printer.printer.PlacementGuide.Action.isReplaceable;
import static net.minecraft.world.level.block.Block.canSupportCenter;

public class CheckingEnvironment {

    public static BlockPos findNearbyFlatBlockToPlaceRedstoneTorch(ClientLevel world, BlockPos blockPos) {

        if ((canSupportCenter(world, blockPos.east(), Direction.UP) && (isReplaceable(world.getBlockState(blockPos.east().above()))) || world.getBlockState(blockPos.east().above()).is(Blocks.REDSTONE_TORCH) && !world.getBlockState(blockPos.east()).isAir())) {
            return blockPos.east();
        } else if ((canSupportCenter(world, blockPos.west(), Direction.UP) && (isReplaceable(world.getBlockState(blockPos.west().above()))) || world.getBlockState(blockPos.west().above()).is(Blocks.REDSTONE_TORCH) && !world.getBlockState(blockPos.west()).isAir())) {
            return blockPos.west();
        } else if ((canSupportCenter(world, blockPos.north(), Direction.UP) && (isReplaceable(world.getBlockState(blockPos.north().above())))  || world.getBlockState(blockPos.north().above()).is(Blocks.REDSTONE_TORCH) && !world.getBlockState(blockPos.north()).isAir())) {
            return blockPos.north();
        } else if ((canSupportCenter(world, blockPos.south(), Direction.UP) && (isReplaceable(world.getBlockState(blockPos.south().above())))  || world.getBlockState(blockPos.south().above()).is(Blocks.REDSTONE_TORCH) && !world.getBlockState(blockPos.south()).isAir())) {
            return blockPos.south();
        }
        return null;
    }

    public static BlockPos findPossibleSlimeBlockPos(ClientLevel world, BlockPos blockPos) {
        if (isReplaceable(world.getBlockState(blockPos.east().above())) && (isReplaceable(world.getBlockState(blockPos.east().above())))) {
            return blockPos.east();
        } else if (isReplaceable(world.getBlockState(blockPos.west().above())) && (isReplaceable(world.getBlockState(blockPos.west().above())))) {
            return blockPos.west();
        } else if (isReplaceable(world.getBlockState(blockPos.south().above())) && (isReplaceable(world.getBlockState(blockPos.south().above())))) {
            return blockPos.south();
        } else if (isReplaceable(world.getBlockState(blockPos.north().above())) && (isReplaceable(world.getBlockState(blockPos.north().above())))) {
            return blockPos.north();
        }
        return null;
    }

    public static boolean has2BlocksOfPlaceToPlacePiston(ClientLevel world, BlockPos blockPos) {
        if (world.getBlockState(blockPos.above()).getDestroySpeed(world, blockPos.above()) == 0) {
            BlockBreaker.breakBlock(world, blockPos.above());
        }
        return isReplaceable(world.getBlockState(blockPos.above())) && isReplaceable(world.getBlockState(blockPos.above().above()));
    }

    public static ArrayList<BlockPos> findNearbyRedstoneTorch(ClientLevel world, BlockPos pistonBlockPos) {
        ArrayList<BlockPos> list = new ArrayList<>();
        if (world.getBlockState(pistonBlockPos.east()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }

        pistonBlockPos = pistonBlockPos.below();
        if (world.getBlockState(pistonBlockPos.east()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }

        pistonBlockPos = pistonBlockPos.below(2);
        if (world.getBlockState(pistonBlockPos.east()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }
        return list;
    }
}
