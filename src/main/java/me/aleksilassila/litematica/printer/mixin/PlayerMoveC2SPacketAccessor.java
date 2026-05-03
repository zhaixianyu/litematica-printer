package me.aleksilassila.litematica.printer.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("x")
    public double getX();

    @Accessor("y")
    public double getY();

    @Accessor("z")
    public double getZ();

    @Accessor("yaw")
    public float getYRot();

    @Accessor("onGround")
    public boolean getOnGround();

    @Accessor("changePosition")
    public boolean changePosition();
}
