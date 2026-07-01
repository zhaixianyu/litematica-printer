package me.aleksilassila.litematica.printer.printer.bedrockUtils;



import fi.dy.masa.malilib.util.InfoUtils;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import static me.aleksilassila.litematica.printer.printer.bedrockUtils.TargetBlock.switchPickaxe;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.getEnchantmentLevel;


//#if MC >= 12105
import net.minecraft.network.HashedStack;
//#endif

//#if MC >= 12006
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
//#endif

//#if MC >= 11700
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
//#endif

public class InventoryManager {
    public static void refresh()
    {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener networkHandler = mc.getConnection();
        if (networkHandler != null && mc.player != null)
        {
            ItemStack uniqueItem = new ItemStack(Items.STONE);

            // Tags with NaN are not equal, so the server will find an inventory desync and send an inventory refresh to the client
            //#if MC >= 12006
            var nbt = new CompoundTag();
            nbt.putDouble("force_sync", Double.NaN);
            CustomData.set(DataComponents.CUSTOM_DATA, uniqueItem, nbt);
            //#else
            //$$ uniqueItem.getOrCreateTag().putDouble("force_resync", Double.NaN);
            //#endif

            AbstractContainerMenu csh = mc.player.containerMenu;
            //#if MC >= 12105
            HashedStack itemStackHash = HashedStack.create(uniqueItem, networkHandler.decoratedHashOpsGenenerator());
            //#endif
            networkHandler.send(new ServerboundContainerClickPacket(
                    csh.containerId,
                    //#if MC >= 11700
                    csh.getStateId(),
                    //#endif
                    (short)-999, (byte)2,
                    ClickType.QUICK_CRAFT,
                    //#if MC < 12105
                    //$$ uniqueItem,
                    //#endif

                    //#if MC >= 11700
                    new Int2ObjectOpenHashMap<>()
                        //#if MC >= 12105
                         , itemStackHash
                        //#endif
                    //#else
                    //$$ csh.backup(mc.player.inventory)
                    //#endif
            ));

            InfoUtils.printActionbarMessage("tweakermore.impl.refreshInventory.refreshed");
        }
    }
    static int i = 0;
    public static boolean switchToItem(ItemLike item) {

//        Item tm;
//        if (item instanceof Item) {
//            tm = (Item) item;
//        } else {
//            tm = item.asItem();
//        }
//        Printer.getPrinter().switchToItems(Minecraft.getInstance().player,new Item[]{tm});

        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();

        int i = playerInventory.findSlotMatchingItem(new ItemStack(item));
        if(item.toString().contains("pickaxe")){
            String string = item.toString();
            int a = 1;
        }
        if ("diamond_pickaxe".equals(item.toString()) || "minecraft:diamond_pickaxe".equals(item.toString())) {
            i = getEfficientTool();
        }else switchPickaxe = false;
        AbstractContainerMenu sc = minecraftClient.player.inventoryMenu;
        if (i != -1) {
            if(!item.toString().contains("pickaxe")){
                for (int i1 = 0; i1 < sc.slots.size(); i1++) {
                    if (ItemStack.isSameItem(sc.slots.get(i1).getItem(),new ItemStack(item))) i = i1;
                }
                sc.clicked(i, 40, ClickType.SWAP, minecraftClient.player);
//                refresh();
            } else {
                if (Inventory.isHotbarSlot(i)) {
                    InventoryUtils.setSelectedSlot(i);
                } else {
                    {
//                        minecraftClient.interactionManager.pickFromInventory(i);
//                        minecraftClient.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.selectedSlot));
                        sc.clicked(i, InventoryUtils.getSelectedSlot(), ClickType.SWAP, minecraftClient.player);
//                        refresh();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static int getEfficientTool() {
        for (int i = 0; i < InventoryUtils.getMainStacks().size(); ++i) {
            if (getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), i) > 45f) {
                return i;
            }
        }
        return -1;
    }

    public static boolean canInstantlyMinePiston() {
        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            if (getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), i) > 45f) {
                return true;
            }
        }
        return false;
    }

    private static float getBlockBreakingSpeed(BlockState block, int slot) {
        Minecraft minecraftClient = Minecraft.getInstance();
        LocalPlayer player = minecraftClient.player;
        ItemStack stack = player.getInventory().getItem(slot);

        float f = stack.getDestroySpeed(block);
        if (f > 1.0F) {
            int i = getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
            float k;
            switch (player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if (player.isEyeInFluid(FluidTags.WATER) && getEnchantmentLevel(stack, Enchantments.AQUA_AFFINITY) <= 0) {
            f /= 5.0F;
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public static int getInventoryItemCount(ItemLike item) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();
        return playerInventory.countItem(item.asItem());
    }

    public static String warningMessage() {
        Minecraft minecraftClient = Minecraft.getInstance();
        if (minecraftClient.gameMode.getPlayerMode().isCreative()) {
            return "bedrockminer.fail.missing.survival";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.PISTON) < 2) {
            return "bedrockminer.fail.missing.piston";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.REDSTONE_TORCH) < 1) {
            return "bedrockminer.fail.missing.redstonetorch";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.SLIME_BLOCK) < 1) {
            return "bedrockminer.fail.missing.slime";
        }

        if (!InventoryManager.canInstantlyMinePiston()) {
            return "bedrockminer.fail.missing.instantmine";
        }
        return null;
    }

}
