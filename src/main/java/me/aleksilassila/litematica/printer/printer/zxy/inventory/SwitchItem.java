package me.aleksilassila.litematica.printer.printer.zxy.inventory;

import fi.dy.masa.malilib.util.InventoryUtils;
import me.aleksilassila.litematica.printer.printer.bedrockUtils.Messager;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.client;

public class SwitchItem {
//    public static boolean switchIng = false;
    public static ItemStack reSwitchItem = null;
    public static Map<ItemStack,ItemStatistics> itemStacks = new HashMap<>();
    public static void removeItem(ItemStack itemStack){
        itemStacks.remove(itemStack);
//        itemStacks.entrySet().removeIf(var -> var.getKey().getItem().equals(itemStack.getItem()));
    }
    public static void syncUseTime(ItemStack itemStack){
        ItemStatistics itemStatistics = itemStacks.get(itemStack);
        if(itemStatistics != null) itemStatistics.syncUseTime();
    }
    public static void newItem(ItemStack itemStack, BlockPos pos, ResourceKey<Level> key, int slot, int shulkerBox){
        if(shulkerBox != -1) itemStacks.put(itemStack,new ItemStatistics(key,pos,slot,shulkerBox));
    }
    public static void openInv(ItemStack itemStack){
        if(client.player == null) return;
        if(!client.player.containerMenu.equals(client.player.inventoryMenu) || Statistics.closeScreen > 0){
            return;
        }
        AbstractContainerMenu sc1 = client.player.containerMenu;
        if (sc1.slots.stream().skip(9).limit(sc1.slots.size()-10)
                .noneMatch(slot -> InventoryUtils.areStacksEqual(slot.getItem(),reSwitchItem))) {
            itemStacks.remove(reSwitchItem);
            reSwitchItem = null;
            return;
        }
        ItemStatistics itemStatistics = itemStacks.get(itemStack);
        if(itemStatistics != null){
            if (itemStatistics.key != null && OpenInventoryPacket.key == null) {
                OpenInventoryPacket.sendOpenInventory(itemStatistics.pos,itemStatistics.key);
                Statistics.closeScreen++;
            }else {
                AbstractContainerMenu sc = client.player.containerMenu;
                //因使用快捷濳影盒打开物品列表后无法更新，所以读取的盒子物品列表没意义。
//                NonNullList<ItemStack> storedItems = InventoryUtils.getStoredItems(sc.slots.get(itemStatistics.shulkerBoxSlot).getStack(), -1);
//                if(storedItems.get(itemStatistics.slot).isEmpty()){
                    try {
//                        Class quickShulker = Class.forName("net.kyrptonaught.quickshulker.client.ClientUtil");
//                        Method checkAndSend = quickShulker.getDeclaredMethod("CheckAndSend",ItemStack.class,int.class);
//                        checkAndSend.invoke(checkAndSend,sc.slots.get(itemStatistics.shulkerBoxSlot).getStack(),itemStatistics.shulkerBoxSlot);
                        client.player.containerMenu.clicked(itemStatistics.shulkerBoxSlot,1, ContainerInput.PICKUP,client.player);
                        Statistics.closeScreen++;
                    } catch (Exception ignored){
                        removeItem(reSwitchItem);
                        reSwitchItem = null;
                    }
//                }
//                for (int i = 9; i < sc.slots.size() && itemStatistics.shulkerBoxSlot != -1; i++) {
//                    ItemStack stack = sc.slots.get(i).getStack();
//                    if (InventoryUtils.getStoredItems(stack,-1).stream().anyMatch(stack1 -> stack1.isEmpty() ||
//                            (InventoryUtils.areStacksEqual(stack1,reSwitchItem) && stack1.getCount() + reSwitchItem.getCount() <= stack1.getMaxCount()))
//                    ) {
//                        try {
//                            Class quickShulker = Class.forName("net.kyrptonaught.quickshulker.client.ClientUtil");
//                            Method checkAndSend = quickShulker.getDeclaredMethod("CheckAndSend",ItemStack.class,int.class);
//                            checkAndSend.invoke(checkAndSend,sc.slots.get(itemStatistics.shulkerBoxSlot).getStack(),i);
//                            Statistics.closeScreen++;
//                            return;
//                        } catch (Exception ignored){}
//                    }
//                }

            }
        }else {
            removeItem(reSwitchItem);
            reSwitchItem = null;
        }
    }
    public static void checkItems(){
        final long[] min = {System.currentTimeMillis()};
        AtomicReference<ItemStack> key = new AtomicReference<>();
        itemStacks.keySet().forEach(k ->{
            long useTime = itemStacks.get(k).useTime;
            if(useTime < min[0]){
                min[0] = useTime;
                key.set(k);
            }
        });
        ItemStack itemStack = key.get();
        if(itemStack != null) {
            reSwitchItem = itemStack;
            openInv(itemStack);
        }else Messager.actionBar("背包已满，请先清理");;
    }
    public static void reSwitchItem(){
        if(client.player == null || reSwitchItem == null) return;
        LocalPlayer player = client.player;
        AbstractContainerMenu sc = player.containerMenu;
        if (sc.equals(player.inventoryMenu)) return;

        List<Integer> sameItem = new ArrayList<>();
        for (int i = 0; i < sc.slots.size(); i++) {
            Slot slot = sc.slots.get(i);
            if(!(slot.container instanceof Inventory) &&
                    InventoryUtils.areStacksEqual(reSwitchItem,slot.getItem()) &&
                    slot.getItem().getCount() < slot.getItem().getMaxStackSize()
            ) sameItem.add(i);
            if(slot.container instanceof Inventory && client.gameMode != null && InventoryUtils.areStacksEqual(slot.getItem(),reSwitchItem)){
                int slot1 = itemStacks.get(reSwitchItem).slot;
                boolean reInv = false;
                //检查记录的槽位是否有物品
                ItemStack stack = sc.slots.get(slot1).getItem();
                if(sc.slots.get(slot1).getItem().isEmpty()){
                    client.gameMode.handleContainerInput(sc.containerId,i, 0, ContainerInput.PICKUP, client.player);
                    client.gameMode.handleContainerInput(sc.containerId,slot1, 0, ContainerInput.PICKUP, client.player);
                    reInv = true;
                } else {
                    int count = reSwitchItem.getCount();
                    client.gameMode.handleContainerInput(sc.containerId,i, 0, ContainerInput.PICKUP, client.player);
                    for (Integer integer : sameItem) {
                        int count1 = sc.slots.get(integer).getItem().getCount();
                        int maxCount = sc.slots.get(integer).getItem().getMaxStackSize();
                        int i1 = maxCount - count1;
                        count -= i1;
                        client.gameMode.handleContainerInput(sc.containerId,integer, 0, ContainerInput.PICKUP, client.player);
                        if (count<=0) reInv = true;
                    }
                }
                removeItem(reSwitchItem);
                reSwitchItem = null;
                player.closeContainer();
                player.closeContainer();
                if(!reInv) {
                    Messager.actionBar("复原库存物品失败");
                }
                client.gameMode.handleContainerInput(sc.containerId,i, 0, ContainerInput.PICKUP, client.player);
                return;
            }
        }
    }
    public static void reSet(){
        reSwitchItem = null;
        itemStacks = new HashMap<>();
    }
    public static void failedSet(){
        itemStacks.remove(reSwitchItem);
        reSwitchItem = null;
    }
    public static class ItemStatistics {
        public ResourceKey<Level> key;
        public BlockPos pos;
        public int slot;
        public int shulkerBoxSlot;
        public long useTime = System.currentTimeMillis();
        public ItemStatistics(ResourceKey<Level> key, BlockPos pos, int slot, int shulkerBox) {
            this.key = key;
            this.pos = pos;
            this.slot = slot;
            this.shulkerBoxSlot = shulkerBox;
        }
        public void syncUseTime(){
            this.useTime = System.currentTimeMillis();
        }
    }
}