package me.aleksilassila.litematica.printer.mixin.openinv;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics.closeScreen;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = {"setScreen"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void setScreen(Screen screen, CallbackInfo ci) {
        if(closeScreen > 0 && /*screen != null &&*/ screen instanceof AbstractContainerScreen<?>){
            closeScreen--;
            ci.cancel();
        }
    }
}
