//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.aleksilassila.litematica.printer.mixin.openinv;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.aleksilassila.litematica.printer.LitematicaMixinMod.INVENTORY;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.QUICKSHULKER;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics.closeScreen;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.*;

@Environment(EnvType.CLIENT)
@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
    @Shadow
    public LocalPlayer player;

    @Shadow
    @Nullable
    public ClientLevel level;

    @Inject(method = {"setScreen"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void setScreen(@Nullable Screen screen, CallbackInfo ci) {
        if(closeScreen > 0 && /*screen != null &&*/ screen instanceof AbstractContainerScreen<?>){
            closeScreen--;
            ci.cancel();
        }
    }
    //鼠标中键从打印机库存或通过快捷濳影盒 取出对应物品
    //#if MC > 12101
    //$$ @WrapOperation(method = "pickBlock",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePickItemFromBlock(Lnet/minecraft/core/BlockPos;Z)V" ))
    //$$ private void doItemPick(MultiPlayerGameMode instance, BlockPos blockPos, boolean bl, Operation<Void> original) {
    //$$     if(level == null) {
    //$$         original.call(instance, blockPos, bl);
    //$$         return;
    //$$     }
    //$$     Item item = level.getBlockState(blockPos).getBlock().asItem();
    //$$     if (player.inventoryMenu.slots.stream().noneMatch(slot -> slot.getItem().getItem().equals(item)) &&
    //$$             !player.getAbilities().instabuild && (INVENTORY.getBooleanValue() || QUICKSHULKER.getBooleanValue())) {
    //$$         remoteItem.add(item);
    //$$         switchItem();
    //$$         return;
    //$$     }
    //$$     original.call(instance, blockPos, bl);
    //$$ }
    //#else
    @WrapOperation(method = "pickBlock",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I" ))
    private int doItemPick(Inventory instance, ItemStack itemStack, Operation<Integer> original) {
        int slotWithStack = original.call(instance, itemStack);
        if(!player.getAbilities().instabuild && (INVENTORY.getBooleanValue() || QUICKSHULKER.getBooleanValue()) && slotWithStack == -1){
            Item item = itemStack.getItem();
            remoteItem.add(item);
            switchItem();
            return -1;
        }
        return slotWithStack;
    }
    //#endif

}
