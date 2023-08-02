package me.aleksilassila.litematica.printer.mixin;

import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacementTweaks.class)
public class MixinTest {
    @Inject(at = @At("HEAD"),method = "onProcessRightClickPre")
    private static void test(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir){

    }
}
