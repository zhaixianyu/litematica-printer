package me.aleksilassila.litematica.printer.printer.qwer;

import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.bedrockUtils.Messager;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.PlayerAction;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.getEnchantmentLevel;

public class PrintWater {

    // 判断方块是否含水
    public static boolean canWaterLogged(BlockState blockState) {
        try {
            if (blockState.is(Blocks.WATER)) {
                return blockState.getValue(LiquidBlock.LEVEL) == 0;
            }else {
                return blockState.getValue(BlockStateProperties.WATERLOGGED);
            }
        } catch (Throwable e) {
            // 这样写应该没问题吧
            return false;
        }
    }// 潜行右键单击
    private static void rightClickBlock(@NotNull LocalPlayer player, BlockPos pos,Printer printer) {
        PlayerAction.setShift(player, true);
        //#if MC > 11802
        printer.client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, true));
        //#else
        //$$ printer.client.useItemOn(player, player.clientWorld, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ofCenter(pos), Direction.DOWN, pos, true));
        //#endif
        PlayerAction.setShift(player, true);
    }
    public static void searchPickaxes(@NotNull LocalPlayer player){
        for (int i = 36; i < player.inventoryMenu.slots.size()-2; i++) {
            ItemStack stack = player.inventoryMenu.slots.get(i).getItem();
            if((stack.is(Items.DIAMOND_PICKAXE)||
                    stack.is(Items.NETHERITE_PICKAXE)) &&
                    !(getEnchantmentLevel(stack, Enchantments.SILK_TOUCH) > 0)){
                InventoryUtils.setSelectedSlot(i-36);
                return;
            }
        }
        Messager.actionBar("快捷栏中没有可用镐子，碎冰速度较慢");
    }
    public static boolean spawnWater(BlockPos pos){
        Minecraft client = ZxyUtils.client;
        //冰碎后无法产生水
        //#if MC > 11904
        BlockState material = client.level.getBlockState(pos.below());
        //#else
        //$$ Material material = client.level.getBlockState(pos.down()).getMaterial();
        //#endif

        if (material.blocksMotion() || material.liquid()) {
            return true;
        }else {
            Messager.actionBar("冰碎后无法产生水");
            return false;
        }
    }
}
