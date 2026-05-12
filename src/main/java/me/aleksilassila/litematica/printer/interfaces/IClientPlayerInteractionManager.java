package me.aleksilassila.litematica.printer.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface IClientPlayerInteractionManager {
    public void rightClickBlock(BlockPos pos, Direction side, Vec3 hitVec);
    public net.minecraft.world.item.ItemStack windowClick_PICKUP(int slot);

    public ItemStack windowClick_QUICK_MOVE(int slot);
}