package me.aleksilassila.litematica.printer.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class MixinHandledScreen {
    @Shadow
    protected Slot focusedSlot;
//    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
//    private void QS$mousePressed(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
//        System.out.println(focusedSlot.id);
//    }
}
