package me.aleksilassila.litematica.printer.mixin.masa;

import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldUtils.class)
public interface WorldUtilsAccessor {
    @Invoker("doEasyPlaceAction")
    static InteractionResult doEasyPlaceAction(Minecraft mc) {
        throw new AssertionError();
    }
}
