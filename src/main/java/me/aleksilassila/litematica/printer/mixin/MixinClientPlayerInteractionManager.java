package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
//#if MC < 11904
//$$ import net.minecraft.world.level.Level;
//#endif

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
	@Shadow
	private Minecraft minecraft;

    @Override
	public void rightClickBlock(BlockPos pos, Direction side, Vec3 hitVec)
	{
		useItemOn(minecraft.player,
				//#if MC < 11902
				//$$ minecraft.level,
				//#endif
				InteractionHand.MAIN_HAND,
			new BlockHitResult(hitVec, side, pos, false));
		useItem(minecraft.player,
				//#if MC < 11902
				//$$ minecraft.level,
				//#endif
				InteractionHand.MAIN_HAND);
	}

//	@Inject(at = @At("TAIL"), method = "interactBlock")
//	private void interactBlock(LocalPlayer player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
//		System.out.println(hitResult.getPos().toString());
//	}

	@Shadow
	public abstract InteractionResult useItemOn(
			LocalPlayer clientPlayerEntity_1,
			//#if MC < 11902
			//$$ ClientLevel world,
			//#endif
			InteractionHand hand_1, BlockHitResult blockHitResult_1);

	@Shadow
	public abstract InteractionResult useItem(Player playerEntity_1,
											  //#if MC < 11902
											  //$$ Level world,
											  //#endif
											  InteractionHand hand_1);
}
