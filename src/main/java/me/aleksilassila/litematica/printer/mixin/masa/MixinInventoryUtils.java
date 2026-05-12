package me.aleksilassila.litematica.printer.mixin.masa;


import fi.dy.masa.litematica.util.InventoryUtils;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.printer.Printer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.*;


@Mixin(InventoryUtils.class)
public class MixinInventoryUtils {
    @Inject(at = @At("TAIL"),method = "schematicWorldPickBlock")
    private static void schematicWorldPickBlock(ItemStack stack, BlockPos pos, Level schematicWorld, Minecraft mc, CallbackInfo ci){
//        System.out.println(cir.getReturnValue().booleanValue());
        if (mc.player != null && !ItemStack.isSameItemSameComponents(mc.player.getMainHandItem(),stack) && (LitematicaMixinMod.INVENTORY.getBooleanValue() || LitematicaMixinMod.QUICKSHULKER.getBooleanValue())) {
            remoteItem.add(stack.getItem());
            switchItem();
        }
    }
}