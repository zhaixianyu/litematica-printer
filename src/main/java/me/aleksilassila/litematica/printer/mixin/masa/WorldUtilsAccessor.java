package me.aleksilassila.litematica.printer.mixin.masa;

import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldUtils.class)
public interface WorldUtilsAccessor {
    @Invoker("doEasyPlaceAction")
    static ActionResult doEasyPlaceAction(Minecraft mc) {
        throw new AssertionError();
    }
}
