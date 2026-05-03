package me.aleksilassila.litematica.printer.printer.bedrockUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//import net.minecraft.block.RedstoneTorchBlock;
//import net.minecraft.core.Direction;

//import java.util.ArrayList;

//import static net.minecraft.block.Block.sideCoversSmallSquare;

public class BlockBreaker {
    public static void breakBlock(ClientWorld world, BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        Minecraft.getInstance().interactionManager.attackBlock(pos, Direction.DOWN);
//        upBreakBlock(world,pos);
    }
    public static void upBreakBlock(ClientWorld world, BlockPos pos) {
        Minecraft.getInstance().interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
    }
}