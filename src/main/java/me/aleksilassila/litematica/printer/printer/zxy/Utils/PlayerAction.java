package me.aleksilassila.litematica.printer.printer.zxy.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;


//#if MC > 12105
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
//#endif

import static me.aleksilassila.litematica.printer.printer.Printer.canBreakBlock;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.canInteracted;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.tick;

public class PlayerAction {
    private static final Minecraft client = Minecraft.getInstance();
    public static boolean isExistPlayer(){
        return client.player != null;
    }
    public static void closeScreen(){
        if (client.player != null) {
            client.player.closeContainer();
        }
    }
    public static void interactBlock(InteractionHand hand, Vec3 vec3d, Direction direction, BlockPos pos, boolean insideBlock, boolean useShift){
        if (useShift) setShift(client.player, true);
        client.gameMode.useItemOn(client.player,
                //#if MC < 11902
                //$$ client.world,
                //#endif
                hand, new BlockHitResult(vec3d, direction, pos, insideBlock));
        client.gameMode.useItem(client.player,
                //#if MC < 11902
                //$$ client.world,
                //#endif
                hand);
        if (useShift) setShift(client.player, false);
    }

    public static void setShift(LocalPlayer player , boolean shift){
        //#if MC > 12105
        Input input = new Input(player.input.keyPresses.forward(), player.input.keyPresses.backward(), player.input.keyPresses.left(), player.input.keyPresses.right(), player.input.keyPresses.jump(), shift, player.input.keyPresses.sprint());
        ServerboundPlayerInputPacket packet = new ServerboundPlayerInputPacket(input);
        //#else
        //$$ ClientCommandC2SPacket packet = new ClientCommandC2SPacket(player, shift ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY);
        //#endif

        player.connection.send(packet);

    }

    static BlockPos breakTargetBlock = null;
    static int startTick = -1;
    //如果返回了null则表示正在挖掘该方块
    public static BlockPos excavateBlock(BlockPos pos){
        if (!canInteracted(pos)) {
            breakTargetBlock = null;
            return null;
        }
        //若无法秒破一个游戏刻挖一次就好
        if (startTick == tick) {
            return null;
        }
        breakTargetBlock = breakTargetBlock != null ? breakTargetBlock : pos;
        if (!waJue(breakTargetBlock)) {
            BlockPos breakTargetBlock1 = breakTargetBlock;
            breakTargetBlock = null;
            return breakTargetBlock1;
        }
        startTick = tick;
        return null;
    }

    public static boolean waJue(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        BlockState currentState = level.getBlockState(pos);
        Block block = currentState.getBlock();
        if (canBreakBlock(pos)) {
            client.gameMode.continueDestroyBlock(pos, Direction.DOWN);
            client.gameMode.stopDestroyBlock();
            return level.getBlockState(pos).is(block);
        }
        return false;
    }

}
