package me.aleksilassila.litematica.printer.printer.bedrockUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
//import net.minecraft.block.RedstoneTorchBlock;
//import net.minecraft.core.Direction;

//import java.util.ArrayList;

//import static net.minecraft.world.level.block.Block.sideCoversSmallSquare;

public class BlockBreaker {
    public static void breakBlock(ClientLevel world, BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        Minecraft.getInstance().gameMode.startDestroyBlock(pos, Direction.DOWN);
//        upBreakBlock(world,pos);
    }
    public static void upBreakBlock(ClientLevel world, BlockPos pos) {
        Minecraft.getInstance().gameMode.continueDestroyBlock(pos, Direction.UP);
    }
}