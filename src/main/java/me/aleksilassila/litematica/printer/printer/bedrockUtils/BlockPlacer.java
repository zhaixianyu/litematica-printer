package me.aleksilassila.litematica.printer.printer.bedrockUtils;

import me.aleksilassila.litematica.printer.printer.zxy.Utils.PlayerAction;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlacer {
    public static void simpleBlockPlacement(TargetBlock tar, BlockPos pos, ItemLike item) {

        Minecraft minecraft = Minecraft.getInstance();

        InventoryManager.switchToItem(item);
//        if(item.equals(Blocks.REDSTONE_TORCH) && minecraftClient.world.getBlockState(pos.down()).isAir()){
////            System.out.println(minecraftClient.world.getBlockState(pos.down()));
//            return;
//        }
        tar.temppos.add(pos);
        BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
        placeBlockWithoutInteractingBlock(minecraft, hitResult);
    }


    private static float yaw;
    private static float pitch;
    private static void resetLook(){
        sendLookPacket(yaw,pitch);
    }
    private static void sendLookPacket(float yaw,float pitch){
        LocalPlayer player = ZxyUtils.client.player;
        if(player == null) return;
        //#if MC > 12101
        Minecraft.getInstance().getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(),player.horizontalCollision));
        //#else
        //$$ MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ServerboundMovePlayerPacket.LookAndOnGround(yaw, pitch, player.isOnGround()));
        //#endif
    }

    public static void pistonPlacement(BlockPos pos, Direction direction) {
        Minecraft minecraftClient = Minecraft.getInstance();
        double x = pos.getX();

        switch (BreakingFlowController.getWorkingMode()) {
            case CARPET_EXTRA://carpet accurateBlockPlacement支持
                x = x + 2 + direction.get3DDataValue() * 2;
                break;
            case VANILLA://直接发包，改变服务端玩家实体视角
                Player player = minecraftClient.player;
                float pitch;
                switch (direction) {
                    case UP:
                        pitch = 90f;
                        break;
                    case DOWN:
                        pitch = -90f;
                        break;
                    default:
                        pitch = 90f;
                        break;
                }
                yaw = player.getYRot();
                BlockPlacer.pitch = player.getXRot();
                BlockPlacer.pitch = player.getVoicePitch();
                sendLookPacket(player.getYRot(1.0f), pitch);
                break;
        }

        Vec3 vec3d = new Vec3(x, pos.getY(), pos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        BlockHitResult hitResult = new BlockHitResult(vec3d, Direction.UP, pos, false);
//        minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, InteractionHand.MAIN_HAND, hitResult);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
        resetLook();
    }

    private static void placeBlockWithoutInteractingBlock(Minecraft minecraftClient, BlockHitResult hitResult) {
        LocalPlayer player = minecraftClient.player;
        ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);

        PlayerAction.interactBlock(InteractionHand.OFF_HAND,hitResult.getBlockPos().getCenter(),hitResult.getDirection(),hitResult.getBlockPos(),hitResult.isInside(),false);
        if (!itemStack.isEmpty() && !player.getCooldowns().isOnCooldown(
                //#if MC > 12101
                itemStack
                //#else
                //$$ itemStack.getItem()
                //#endif
        )) {
            UseOnContext itemUsageContext = new UseOnContext(player, InteractionHand.OFF_HAND, hitResult);
            itemStack.useOn(itemUsageContext);

        }
    }
}
