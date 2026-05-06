package me.aleksilassila.litematica.printer.mixin.masa;

import fi.dy.masa.litematica.util.InventoryUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InventoryUtils.class)
public interface Litematica_InventoryUtilsMixin {
    @Invoker("getPickBlockTargetSlot")
    public static int getPickBlockTargetSlot(Player player){
        return -1;
    };
    @Invoker("getEmptyPickBlockableHotbarSlot")

    public static int getEmptyPickBlockableHotbarSlot(Inventory inventory){
        return -1;
    };
}