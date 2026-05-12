package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.printer.zxy.inventory.SwitchItem;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.*;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.SwitchItem.reSwitchItem;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.*;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(at = @At("TAIL"),method = "handleContainerContent")
    public void onInventory(ClientboundContainerSetContentPacket packet, CallbackInfo ci){
         Minecraft mc = Minecraft.getInstance();
        if(isOpenHandler){
            switchInv();
        }
        if(reSwitchItem != null ){
            SwitchItem.reSwitchItem();
        }

        if (client.player != null && printerMemoryAdding) {
            client.player.closeContainer();
        }
//        if(QuickShulkerUtils.waitForTheItemToBeSwitched != null) QuickShulkerUtils.switchItem(QuickShulkerUtils.targetSlot);
        if(num == 1 || num == 3)ZxyUtils.syncInv();
     }
}
