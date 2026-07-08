package me.aleksilassila.litematica.printer.printer.zxy.inventory;

import fi.dy.masa.litematica.config.Configs;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import me.aleksilassila.litematica.printer.mixin.masa.Litematica_InventoryUtilsMixin;
import me.aleksilassila.litematica.printer.mixin.openinv.ShulkerBoxBlockAccessor;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.bedrockUtils.Messager;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;

//#if MC > 11904
import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils;
import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.SearchItem;
import net.minecraft.world.phys.AABB;
import red.jackf.chesttracker.api.providers.InteractionTracker;
//#else
//$$     import me.aleksilassila.litematica.printer.printer.zxy.memory.Memory;
//$$     import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryDatabase;
//$$     import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryUtils;
//#if MC > 11902
//$$ import net.minecraft.core.registries.Registries;
//#else
//#endif
//#endif


import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static me.aleksilassila.litematica.printer.LitematicaMixinMod.PRINT_CHECK;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics.closeScreen;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics.loadChestTracker;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.client;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket.openIng;

public class InventoryUtils {
    public static boolean isInventory(Level world, BlockPos pos) {
        return fi.dy.masa.malilib.util.InventoryUtils.getInventory(world, pos) != null;
    }
    public static boolean hasItem(Item item){
        if (client.player.isCreative()) return true;
        Inventory inventory = client.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem().equals(item)) return true;
        }
        return false;
    }

    public static boolean canOpenInv(BlockPos pos) {
        if (client.level != null) {
            BlockState blockState = client.level.getBlockState(pos);
            BlockEntity blockEntity = client.level.getBlockEntity(pos);
            boolean isInventory = InventoryUtils.isInventory(client.level, pos);
            try {
                if ((isInventory && blockState.getMenuProvider(client.level, pos) == null) ||
                        (blockEntity instanceof ShulkerBoxBlockEntity entity &&
                                !ShulkerBoxBlockAccessor.canOpen(blockState,client.level,pos,entity))) {
                    return false;
                } else if (!isInventory) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static LinkedHashSet<Item> remoteItem = new LinkedHashSet<>();
    public static boolean isOpenHandler = false;

    public static boolean switchItem() {
        if (!remoteItem.isEmpty() && !isOpenHandler && !openIng && OpenInventoryPacket.key == null) {
            LocalPlayer player = client.player;
            AbstractContainerMenu sc = player.containerMenu;
            if (!player.containerMenu.equals(player.inventoryMenu)) return false;
            //排除合成栏 装备栏 副手
            if (PRINT_CHECK.getBooleanValue() && sc.slots.stream().skip(9).limit(sc.slots.size() - 10).noneMatch(slot -> slot.getItem().isEmpty())
                    && (LitematicaMixinMod.QUICKSHULKER.getBooleanValue() || LitematicaMixinMod.INVENTORY.getBooleanValue())) {
                SwitchItem.checkItems();
                return true;
            }
            if (LitematicaMixinMod.QUICKSHULKER.getBooleanValue() && openShulker(remoteItem)) {
                return true;
            } else if (LitematicaMixinMod.INVENTORY.getBooleanValue()) {
                for (Item item : remoteItem) {
                    //#if MC >= 12001
                    //#if MC > 12004
                    MemoryUtils.currentMemoryKey = client.level.dimension().identifier();
                    //#else
                    //$$ MemoryUtils.currentMemoryKey = client.level.dimensionTypeId().location();
                    //#endif
                    MemoryUtils.itemStack = new ItemStack(item);
                    if (SearchItem.search(true)) {
                        closeScreen++;
                        isOpenHandler = true;
                        Printer.printerMemorySync = true;
                        return true;
                    }
                    //#else
                    //$$
                    //$$    MemoryDatabase database = MemoryDatabase.getCurrent();
                    //$$    if (database != null) {
                    //$$        for (ResourceLocation dimension : database.getDimensions()) {
                    //$$            for (Memory memory : database.findItems(item.getDefaultInstance(), dimension)) {
                    //$$                MemoryUtils.setLatestPos(memory.getPosition());
                                   //#if MC > 11902
                                   //$$ OpenInventoryPacket.sendOpenInventory(memory.getPosition(), ResourceKey.create(Registries.DIMENSION, dimension));
                                   //#else
                                   //$$ OpenInventoryPacket.sendOpenInventory(memory.getPosition(), ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension));
                                   //#endif
                    //$$                if(closeScreen == 0)closeScreen++;
                    //$$                Printer.printerMemorySync = true;
                    //$$                isOpenHandler = true;
                    //$$                return true;
                    //$$            }
                    //$$        }
                    //$$    }
                    //#endif
                }
                remoteItem = new LinkedHashSet<>();
                isOpenHandler = false;
            }
        }
        return false;
    }

    static int shulkerBoxSlot = -1;

    public static void switchInv() {
//        if(true) return;

        LocalPlayer player = Minecraft.getInstance().player;
        AbstractContainerMenu sc = player.containerMenu;
        if (sc.equals(player.inventoryMenu)) {
            return;
        }
        NonNullList<Slot> slots = sc.slots;
        for (Item item : remoteItem) {
            for (int y = 0; y < slots.get(0).container.getContainerSize(); y++) {
                if (slots.get(y).getItem().getItem().equals(item)) {

                    String[] str = Configs.Generic.PICK_BLOCKABLE_SLOTS.getStringValue().split(",");
                    if (str.length == 0) return;
                    for (String s : str) {
                        if (s == null) break;
                        try {
                            int c = Integer.parseInt(s) - 1;
                            if (BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(c).getItem()).toString().contains("shulker_box") &&
                                    LitematicaMixinMod.QUICKSHULKER.getBooleanValue()) {
                                Messager.actionBar("濳影盒占用了预选栏");
                                continue;
                            }

                            if (OpenInventoryPacket.key != null) {
                                SwitchItem.newItem(slots.get(y).getItem(), OpenInventoryPacket.pos, OpenInventoryPacket.key, y, -1);
                            } else SwitchItem.newItem(slots.get(y).getItem(), null, null, y, shulkerBoxSlot);
                            int a = Litematica_InventoryUtilsMixin.getEmptyPickBlockableHotbarSlot(player.getInventory()) == -1 ?
                                    Litematica_InventoryUtilsMixin.getPickBlockTargetSlot(player) :
                                    Litematica_InventoryUtilsMixin.getEmptyPickBlockableHotbarSlot(player.getInventory());
                            c = a == -1 ? c : a;
                            ZxyUtils.switchPlayerInvToHotbarAir(c);
                            fi.dy.masa.malilib.util.InventoryUtils.swapSlots(sc, y, c);
                            InventoryUtils.setSelectedSlot(c);
                            player.closeContainer();
                            //刷新濳影盒
                            if (shulkerBoxSlot != -1) {
                                client.gameMode.handleContainerInput(sc.containerId,shulkerBoxSlot, 0, ContainerInput.PICKUP, client.player);
                                client.gameMode.handleContainerInput(sc.containerId,shulkerBoxSlot, 0, ContainerInput.PICKUP, client.player);
                            }
                            shulkerBoxSlot = -1;
                            isOpenHandler = false;
                            remoteItem = new LinkedHashSet<>();
                            return;
                        } catch (Exception e) {
                            System.out.println("切换物品异常");
                        }
                    }
                }
            }
        }
        shulkerBoxSlot = -1;
        remoteItem = new LinkedHashSet<>();
        isOpenHandler = false;
        AbstractContainerMenu sc2 = player.containerMenu;
        if (!sc2.equals(player.inventoryMenu)) {
            player.closeContainer();
        }
    }

    public static void setSelectedSlot(int slot) {

        if (client.player != null) {
            //#if MC > 12104
            client.player.getInventory().setSelectedSlot(slot);
            //#else
            //$$ client.player.getInventory().selected = slot;
            //#endif
        }
    }

    public static int getSelectedSlot() {

        if (client.player != null) {
            //#if MC > 12104
            return client.player.getInventory().getSelectedSlot();
            //#else
            //$$ return client.player.getInventory().selected;
            //#endif
        } else return -1;
    }

    public static NonNullList<ItemStack> getMainStacks() {
        if (client.player != null) {
            //#if MC > 12104
            return client.player.getInventory().getNonEquipmentItems();
            //#else
            //$$ return client.player.getInventory().items;
            //#endif
        }else return NonNullList.create();
    }

    public static boolean switchToItems(LocalPlayer player, Item[] items) {
        if (items == null) return false;
        Inventory inv = Implementation.getInventory(player);
        //inv.getMainHandStack()  信息滞后 如果服务器有延迟这个获取的信息可能是错误的
//        for (Item item : items) {
//            if (inv.getMainHandStack().getItem() == item) {
//                return;
//            }
//        }
        for (Item item : items) {
            if (Implementation.getAbilities(player).instabuild) {
                fi.dy.masa.litematica.util.InventoryUtils.setPickedItemToHand(new ItemStack(item), client);
                client.gameMode.handleCreativeModeItemAdd(client.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + getSelectedSlot());
                return true;
            } else {
                int slot = -1;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    if (inv.getItem(i).getItem() == item && inv.getItem(i).getCount() > 0)
                        slot = i;
                }
                if (slot != -1) {
                    Printer.yxcfItem = inv.getItem(slot);
                    Printer.getPrinter().swapHandWithSlot(player, slot);
                    return true;
                }
            }
        }
        return false;
    }

    private static Method method;

    static {
        try {
            method = Class.forName("net.kyrptonaught.quickshulker.client.ClientUtil").getDeclaredMethod("CheckAndSend", ItemStack.class, int.class);
        } catch (Exception ignored) {
            method = null;
        }
    }

    static boolean openShulker(HashSet<Item> items) {
        for (Item item : items) {
            AbstractContainerMenu sc = Minecraft.getInstance().player.inventoryMenu;
            for (int i = 9; i < sc.slots.size(); i++) {
                ItemStack stack = sc.slots.get(i).getItem();
                String itemid = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (itemid.contains("shulker_box") && stack.getCount() == 1) {
                    NonNullList<ItemStack> items1 = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(stack, -1);
                    if (items1.stream().anyMatch(s1 -> s1.getItem().equals(item))) {
                        try {
                            shulkerBoxSlot = i;
//                            ClientUtil.CheckAndSend(stack,i);
                            //#if MC >= 12001
                            if (loadChestTracker) InteractionTracker.INSTANCE.clear();
                            //#endif
                            method.invoke(method, stack, i);
                            closeScreen++;
                            isOpenHandler = true;
                            return true;
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String getItemName(ItemStack itemStack){
        //#if MC > 12101
        return itemStack.getItemName().getString();
        //#else
        //$$ return itemStack.getDescriptionId();
        //#endif
    }
}
