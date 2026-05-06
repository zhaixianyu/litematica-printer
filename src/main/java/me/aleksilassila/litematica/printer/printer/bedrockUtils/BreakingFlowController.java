package me.aleksilassila.litematica.printer.printer.bedrockUtils;

import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.PlacementHandler;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.PlayerAction;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

import static me.aleksilassila.litematica.printer.printer.Printer.bedrockModeRange;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.*;

public class BreakingFlowController {
    public static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    //加入破坏列表
    public static void addBlockPosToList(BlockPos pos) {

        if (cachedTargetBlockList.size() > 5) return;

        ClientLevel world = Minecraft.getInstance().level;
//        if (world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
        Minecraft minecraftClient = Minecraft.getInstance();

        String haveEnoughItems = InventoryManager.warningMessage();
        if (haveEnoughItems != null) {
            Messager.actionBar(haveEnoughItems);
            return;
        }
        for (TargetBlock block : cachedTargetBlockList) {
            if (pos.equals(block.getBlockPos()) || pos.equals(block.getnyk()) || pos.equals(block.geths()))
                return;
        }
        if (!minecraftClient.level.getBlockState(pos.above()).isAir() || !minecraftClient.level.getBlockState(pos.above().above()).isAir()) {
            if (!Printer.bedrockModeTarget(minecraftClient.level.getBlockState(pos.above())))
                addPosList(pos.above());
            if (!Printer.bedrockModeTarget(minecraftClient.level.getBlockState(pos.above().above())))
                addPosList(pos.above().above());
            return;
        }

        cachedTargetBlockList.add(new TargetBlock(pos, world));
        //Suggest also an english version, for debug reasons.
//                System.out.println("新任务");
//            }
    }

    public static ArrayList<BlockPos> poslist = new ArrayList<>();

    public static void addPosList(BlockPos pos) {
        if (poslist.stream().noneMatch(pos1 -> pos1.equals(pos))) poslist.add(pos);
    }

    static void deleteBlock() {
        for (int i = 0; i < poslist.size(); i++) {
            BlockPos blockPos = poslist.get(i);

            if (Minecraft.getInstance().level.getBlockState(blockPos).isAir() && ZxyUtils.bedrockCanInteracted(blockPos, bedrockModeRange())) {
                InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
                //#if MC < 11902
                //$$ client.useItemOn(client.player,client.world, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ofCenter(blockPos), Direction.UP, poslist.get(i), false));
                //#else
                client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, poslist.get(i), false));
                //#endif
                if (Minecraft.getInstance().level.getBlockState(blockPos).isAir()) {
                    poslist.remove(i);
                    i--;
                    continue;
                }
            }
            if (!ZxyUtils.bedrockCanInteracted(blockPos, bedrockModeRange() * 2)) {
                poslist.remove(i);
                i--;
                continue;
            }


            if (!ZxyUtils.bedrockCanInteracted(blockPos, bedrockModeRange())) continue;
            if (!Minecraft.getInstance().level.getBlockState(blockPos).isAir()) {
//                BlockBreaker.breakBlock(Minecraft.getInstance().world, poslist.get(i));
                InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
                PlayerAction.waJue(blockPos);
            }


        }
    }

    public static void tick() {
        deleteBlock();
        //检测是否符合条件 物品是否带起 游戏模式 是否有信标
        if (InventoryManager.warningMessage() != null) {
            return;
        }

        Minecraft minecraftClient = Minecraft.getInstance();
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

//            if (!blockInPlayerRange(selectedBlock.getBlockPos(), player, 5f)) {
            if (!ZxyUtils.bedrockCanInteracted(selectedBlock.getBlockPos(), getRage() - 1.5)) {
                cachedTargetBlockList.remove(i);
                continue;
            }
            //玩家切换世界，或离目标方块太远时，删除所有缓存的任务
            if (selectedBlock.getWorld() != Minecraft.getInstance().level) {
                cachedTargetBlockList = new ArrayList<TargetBlock>();
                break;
            }

//            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 5f)) {
//            if (DataManager.getRenderLayerRange().isPositionWithinRange(selectedBlock.getBlockPos())) {
            //#if MC > 12006
            ItemStack mainHandStack = client.player.getMainHandItem();
            cachedTargetBlockList.stream().filter( targetBlock -> targetBlock.getStatus() == TargetBlock.Status.EXTENDED).forEach(TargetBlock::tick);
            //#endif
            TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
            if (status == TargetBlock.Status.RETRACTING) {
                continue;
            } else if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.RETRACTED) {
                for (BlockPos temppo : cachedTargetBlockList.get(i).temppos) {
                    if (!minecraftClient.level.getBlockState(temppo).isAir()) addPosList(temppo);
                }
                cachedTargetBlockList.remove(i);
            }/* else {
                    break;
                }*/

//            }
        }
        //#if MC > 12006
        if (cachedTargetBlockList.stream().anyMatch(targetBlock -> targetBlock.getStatus() == TargetBlock.Status.EXTENDED)) {
            InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
            TargetBlock.switchPickaxe = true;
        }
        //#endif
    }

//    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
//        return blockPos.isWithinDistance(player.getPos(), range);
//    }

    public static WorkingMode getWorkingMode() {
        if (LitematicaMixinMod.EASY_MODE.getBooleanValue() && PlacementHandler.getEffectiveProtocolVersion() == EasyPlaceProtocol.V2) {
            return WorkingMode.CARPET_EXTRA;
        }else return WorkingMode.VANILLA;
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos) {
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().distManhattan(pos) == 0) {
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff() {
    }


    //测试用的。使用原版模式已经足以满足大多数需求。
    //just for test. The VANILLA mode is powerful enough.
    enum WorkingMode {
        CARPET_EXTRA,
        VANILLA,
        MANUALLY;
    }
}
