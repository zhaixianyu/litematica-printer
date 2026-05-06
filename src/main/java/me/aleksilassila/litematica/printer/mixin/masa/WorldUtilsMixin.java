package me.aleksilassila.litematica.printer.mixin.masa;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.aleksilassila.litematica.printer.LitematicaMixinMod.USE_EASY_MODE;
import static me.aleksilassila.litematica.printer.printer.Printer.easyPos;

@Mixin(WorldUtils.class)
public class WorldUtilsMixin {

    @WrapOperation(at= @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getGenericTrace(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZZZ)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;" ),method = "doEasyPlaceAction")
    private static RayTraceUtils.RayTraceWrapper doSchematicWorldPickBlock(Level level, net.minecraft.world.entity.Entity dist, double verifier, boolean posList, boolean traceMismatch, boolean worldClient, Operation<RayTraceUtils.RayTraceWrapper> original){
        if (USE_EASY_MODE.getBooleanValue() && easyPos != null) {
            return new RayTraceUtils.RayTraceWrapper(RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK,new BlockHitResult(Vec3.atLowerCornerOf(easyPos),Direction.UP,easyPos,false));
        }else {
            return original.call(level, dist, verifier, posList, traceMismatch, worldClient);
        }

    }
    @WrapOperation(at= @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getFurthestSchematicWorldTraceBeforeVanilla(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;D)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;"),method = "doEasyPlaceAction")
    private static RayTraceUtils.RayTraceWrapper doSchematicWorldPickBlock2(Level vanillaHitResult, net.minecraft.world.entity.Entity dist, double trace, Operation<RayTraceUtils.RayTraceWrapper> original){
        if (USE_EASY_MODE.getBooleanValue() && easyPos != null) {
            return new RayTraceUtils.RayTraceWrapper(RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK,new BlockHitResult(Vec3.atLowerCornerOf(easyPos),Direction.UP,easyPos,false));
        }else {
            return original.call(vanillaHitResult, dist, trace);
        }
    }
}
