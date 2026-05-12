package me.aleksilassila.litematica.printer.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("x")
    public double getX();

    @Accessor("y")
    public double getY();

    @Accessor("z")
    public double getZ();

    @Accessor("yRot")
    public float getYRot();

    @Accessor("onGround")
    public boolean getOnGround();

    @Accessor("hasPos")
    public boolean changePosition();
}
