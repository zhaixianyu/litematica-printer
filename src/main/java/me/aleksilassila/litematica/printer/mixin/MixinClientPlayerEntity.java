package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.printer.PlacementGuide;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.BlockTask;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

//#if MC >= 12001
//$$ import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils;
//#endif

import static me.aleksilassila.litematica.printer.printer.Printer.isEnablePrinter;
import static me.aleksilassila.litematica.printer.printer.UpdateChecker.checkForUpdates;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {
    @Final
	@Shadow
	protected Minecraft minecraft;

	@Inject(at = @At("HEAD"), method = "closeContainer")
	public void close(CallbackInfo ci) {
		//#if MC >= 12001
		//$$ 	if(Statistics.loadChestTracker) MemoryUtils.saveMemory(((LocalPlayer)(Object)this).containerMenu);
		//$$ 	OpenInventoryPacket.reSet();
		//#endif
	}
	@Inject(at = @At("TAIL"), method = "tick")
	public void tick(CallbackInfo ci) {
		Printer printer = Printer.getPrinter();
		ZxyUtils.tick();
		printer.myTick();
		if(!(isEnablePrinter())){
			PlacementGuide.posMap = new HashMap<>();
			printer.myBox = null;
			printer.replaceTaskMap = new HashMap<>();
			return;
		}
		printer.tick();
		checkForUpdates();
        BlockTask.BlockTaskManager.tick();
	}
}