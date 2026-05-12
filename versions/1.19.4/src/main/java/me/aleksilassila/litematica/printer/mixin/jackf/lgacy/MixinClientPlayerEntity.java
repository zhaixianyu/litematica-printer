package me.aleksilassila.litematica.printer.mixin.jackf.lgacy;

import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics;
import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.printerMemoryAdding;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {
    @Shadow @Final protected Minecraft minecraft;
    @Inject(at = @At("HEAD"), method = "clientSideCloseContainer")
    public void closeScreen(CallbackInfo ci) {
        BlockPos pos = MemoryUtils.getLatestPos();
        if(Statistics.loadChestTracker && LitematicaMixinMod.INVENTORY.getBooleanValue() &&
                (LitematicaMixinMod.TOGGLE_PRINTING_MODE.getBooleanValue() || LitematicaMixinMod.PRINT.getKeybind().isPressed() || printerMemoryAdding || Printer.printerMemorySync) &&(
                pos != null || MemoryUtils.getMemoryPos() != null)){
            if(!minecraft.player.containerMenu.equals(minecraft.player.inventoryMenu)){
                MemoryUtils.handleItemsFromScreen(minecraft.player.containerMenu);
            }
        }
        OpenInventoryPacket.reSet();
    }
}
