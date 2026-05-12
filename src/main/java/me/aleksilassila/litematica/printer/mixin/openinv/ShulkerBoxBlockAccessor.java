package me.aleksilassila.litematica.printer.mixin.openinv;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(ShulkerBoxBlock.class)
public interface ShulkerBoxBlockAccessor {
    @Invoker("canOpen")
    static boolean canOpen(BlockState blockState, Level level, BlockPos blockPos, ShulkerBoxBlockEntity shulkerBoxBlockEntity){
        return new Random(10).nextInt() % 2 == 0;
    }
}
