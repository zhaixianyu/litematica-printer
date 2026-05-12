package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import me.aleksilassila.litematica.printer.printer.Printer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;


import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC > 12001
//$$ import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
//$$ @Mixin(value = ClientCommonPacketListenerImpl.class)
//#else
import net.minecraft.client.multiplayer.ClientPacketListener;
@Mixin(ClientPacketListener.class)
//#endif
public class ClientCommonNetworkHandlerMixin {
    @Final
    @Shadow
    protected Connection connection;

    @Final
    @Shadow
    protected Minecraft minecraft;

    /**
     * @author 6
     * @reason 6
     */

    //#if MC < 12004
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"),method = "send(Lnet/minecraft/network/protocol/Packet;)V", cancellable = true)
    //#else
    //$$ @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "send", cancellable = true)
    //#endif
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (Printer.currentAction == null) {
            return;
        }

        Direction direction = Printer.currentAction.lookDirection;
        if (direction != null) {
            if (packet instanceof ServerboundMovePlayerPacket.PosRot full) {
                Packet<?> fixedPacket = Implementation.getFixedLookPacket(minecraft.player, full, Printer.currentAction);
                if (fixedPacket != null) {
                    this.connection.send(fixedPacket);
                    ci.cancel();
                }
            } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
                ci.cancel();
            }
        }
    }
}
