package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.PlayerAction.interactBlock;

public class Test {
    public static void main(String[] args) {

    }
    public static void t1(){
        Minecraft client = ZxyUtils.client;
        LocalPlayer player = client.player;
        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(1.0f,0.0f,player.isOnGround()
                //#if MC > 12101
                ,player.horizontalCollision
                //#endif
        ));
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        Vec3 offset = client.crosshairTarget.getPos();
        BlockPos blockPos = new BlockPos((int) offset.x, (int) offset.y, (int) offset.z).up();
        interactBlock(Hand.MAIN_HAND,new Vec3(0.5,0.5,0.5),Direction.UP,blockPos,false,false);
    }
}