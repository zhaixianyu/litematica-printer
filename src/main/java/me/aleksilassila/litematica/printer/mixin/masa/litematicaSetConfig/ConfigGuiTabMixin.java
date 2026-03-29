package me.aleksilassila.litematica.printer.mixin.masa.litematicaSetConfig;


import fi.dy.masa.litematica.gui.GuiConfigs;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GuiConfigs.ConfigGuiTab.class,priority = 500)
public class ConfigGuiTabMixin {

    @Inject(method = "values", at = @At("RETURN"), cancellable = true, remap = false)
    private static void values(CallbackInfoReturnable<GuiConfigs.ConfigGuiTab[]> cir) {
        GuiConfigs.ConfigGuiTab[] returnValue = cir.getReturnValue();
        GuiConfigs.ConfigGuiTab[] arr = new GuiConfigs.ConfigGuiTab[returnValue.length + 1];
        System.arraycopy(returnValue, 0, arr, 0, returnValue.length);
        arr[arr.length - 1] = LitematicaMixinMod.PRINTER_TAB_KEY;
        cir.setReturnValue(arr);
    }
}
