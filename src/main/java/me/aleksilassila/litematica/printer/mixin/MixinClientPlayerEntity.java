package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.printer.PlacementGuide;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.BlockTask;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

//#if MC >= 12001
import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils;
//#endif

import static me.aleksilassila.litematica.printer.printer.Printer.isEnablePrinter;
import static me.aleksilassila.litematica.printer.printer.UpdateChecker.checkForUpdates;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Final
	@Shadow
	protected MinecraftClient client;

	@Inject(at = @At("HEAD"), method = "closeHandledScreen")
	public void close(CallbackInfo ci) {
		//#if MC >= 12001
			if(Statistics.loadChestTracker) MemoryUtils.saveMemory(((ClientPlayerEntity)(Object)this).currentScreenHandler);
			OpenInventoryPacket.reSet();
		//#endif
	}
	@Inject(at = @At("TAIL"), method = "tick")
	public void tick(CallbackInfo ci) {
		Printer printer = Printer.getPrinter();
		ZxyUtils.tick();
		printer.myTick();
		if(!(isEnablePrinter())){
			PlacementGuide.posMap = new HashMap<>();
			printer.basePos = null;
			printer.replaceTaskMap = new HashMap<>();
			return;
		}
		printer.tick();
		checkForUpdates();
        BlockTask.BlockTaskManager.tick();
	}
}