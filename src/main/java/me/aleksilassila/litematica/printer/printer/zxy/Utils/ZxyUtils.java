package me.aleksilassila.litematica.printer.printer.zxy.Utils;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.printer.Printer;
import me.aleksilassila.litematica.printer.printer.State;

import me.aleksilassila.litematica.printer.printer.bedrockUtils.BreakingFlowController;
import me.aleksilassila.litematica.printer.printer.bedrockUtils.Messager;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.SwitchItem;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.*;
import java.util.function.Consumer;

//#if MC < 12101
//$$ import net.minecraft.world.item.enchantment.EnchantmentHelper;
//#endif

//#if MC >= 12105
import net.minecraft.network.HashedStack;
//#endif

//#if MC >= 12001
import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils;
//#else
//$$ import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryUtils;
//#endif
//#if MC >= 12006
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
//#endif
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SYNC_INVENTORY_CHECK;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.SYNC_INVENTORY_COLOR;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.canOpenInv;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.OpenInventoryPacket.*;
import static me.aleksilassila.litematica.printer.printer.zxy.Utils.Statistics.closeScreen;

public class ZxyUtils {
    //旧版箱子追踪
    public static boolean qw = false;
    public static int currWorldId = 0;

    @NotNull
    public static Minecraft client = Minecraft.getInstance();
    public static LinkedList<BlockPos> invBlockList = new LinkedList<>();
    public static boolean printerMemoryAdding = false;
    public static boolean syncPrinterInventory = false;
    public static String syncInventoryId = "syncInventory";
    public static int tick = 0;

    public static void startAddPrinterInventory(){
        getReadyColor();
        if (LitematicaMixinMod.INVENTORY.getBooleanValue() && !printerMemoryAdding) {
            printerMemoryAdding = true;
            //#if MC >= 12001
            if (MemoryUtils.PRINTER_MEMORY == null) MemoryUtils.createPrinterMemory();
            //#endif

            for (String string : LitematicaMixinMod.INVENTORY_LIST.getStrings()) {
                invBlockList.addAll(Printer.getPrinter().siftBlock(string).stream().filter(InventoryUtils::canOpenInv).toList());
            }
            highlightPosList.addAll(invBlockList);
        }
    }
    public static void addInv() {
        if (printerMemoryAdding && !openIng && OpenInventoryPacket.key == null) {
            if (invBlockList.isEmpty()) {
                printerMemoryAdding = false;
                client.gui.setOverlayMessage(Component.literal("打印机库存添加完成"), false);
                return;
            }
            client.gui.setOverlayMessage(Component.literal("添加库存中"), false);
            for (BlockPos pos : invBlockList) {
                if (client.level != null) {
                    //#if MC < 12001
                    //$$ MemoryUtils.setLatestPos(pos);
                    //#endif
                    closeScreen++;
                    OpenInventoryPacket.sendOpenInventory(pos, client.level.dimension());
//                    ((IClientPlayerInteractionManager) client.gameMode)
//                            .rightClickBlock(pos,Direction.UP ,new Vec3(pos.getX(), pos.getY(), pos.getZ()) );
                }
                invBlockList.remove(pos);
                highlightPosList.remove(pos);
                break;
            }
        }
    }

    public static LinkedList<BlockPos> syncPosList = new LinkedList<>();
    public static ArrayList<ItemStack> targetBlockInv;
    public static int num = 0;
    static BlockPos blockPos = null;
    static Set<BlockPos> highlightPosList = new LinkedHashSet<>();
    static Map<ItemStack,Integer> targetItemsCount = new HashMap<>();
    static Map<ItemStack,Integer> playerItemsCount = new HashMap<>();

    private static void getReadyColor(){
        HighlightBlockRenderer.createHighlightBlockList(syncInventoryId,SYNC_INVENTORY_COLOR);
        highlightPosList = HighlightBlockRenderer.getHighlightBlockPosList(syncInventoryId);
    }

    public static void startOrOffSyncInventory() {
        getReadyColor();
        if (client.hitResult != null && client.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && syncPosList.isEmpty()) {
            BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
            net.minecraft.world.level.block.Block block = null;
            if (client.level != null) {
                block = client.level.getBlockState(pos).getBlock();
                if(!canOpenInv(pos)){
                    Messager.actionBar("打开容器失败");
                    return;
                }
            }
            String blockName = BuiltInRegistries.BLOCK.getKey(block).toString();
//            String blockName = Registries.BLOCK.getId(block).toString();
            Printer.getPrinter();
            syncPosList.addAll(Printer.getPrinter().siftBlock(blockName));
            if (!syncPosList.isEmpty()) {
                if (client.player == null) return;
                client.player.closeContainer();
                if (!openInv(pos,false)){
                    syncPosList = new LinkedList<>();
                    return;
                }
                highlightPosList.addAll(syncPosList);
                closeScreen++;
                num = 1;
            }
        } else if(!syncPosList.isEmpty()){
            highlightPosList.removeAll(syncPosList);
            syncPosList = new LinkedList<>();
            if (client.player != null) client.player.clientSideCloseContainer();
            num = 0;
            client.gui.setOverlayMessage(Component.literal("已取消同步"), false);
        }
    }
    public static boolean openInv(BlockPos pos,boolean ignoreThePrompt){
        if(LitematicaMixinMod.INVENTORY.getBooleanValue() && OpenInventoryPacket.key == null) {
            OpenInventoryPacket.sendOpenInventory(pos, client.level.dimension());
            return true;
        } else {
            if (client.player != null && !canInteracted(5,Vec3.atCenterOf(pos))) {
                if(!ignoreThePrompt) client.gui.setOverlayMessage(Component.literal("距离过远无法打开容器"), false);
                return false;
            }
            if (client.gameMode != null){
                //#if MC < 11902
                //$$ client.gameMode.useItemOn(client.player, client.level, InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN,pos,false));
                //#else
                client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN,pos,false));
                //#endif
                return true;
            } else return false;
        }
    }
    public static void itemsCount(Map<ItemStack,Integer> itemsCount , ItemStack itemStack){
        // 判断是否存在可合并的键
        Optional<Map.Entry<ItemStack, Integer>> entry = itemsCount.entrySet().stream()
                .filter(e -> ItemStack.isSameItemSameComponents(e.getKey(), itemStack))
                .findFirst();

        if (entry.isPresent()) {
            // 更新已有键对应的值
            Integer count = entry.get().getValue();
            count += itemStack.getCount();
            itemsCount.put(entry.get().getKey(), count);
        } else {
            // 添加新键值对
            itemsCount.put(itemStack, itemStack.getCount());
        }
    }

    public static void syncInv() {
        switch (num) {
            case 1 -> {
                //按下热键后记录看向的容器 开始同步容器 只会触发一次
                targetBlockInv = new ArrayList<>();
                targetItemsCount = new HashMap<>();
                if (client.player != null && (!LitematicaMixinMod.INVENTORY.getBooleanValue() || openIng) && !client.player.containerMenu.equals(client.player.inventoryMenu)) {
                    for (int i = 0; i < client.player.containerMenu.slots.get(0).container.getContainerSize(); i++) {
                        ItemStack copy = client.player.containerMenu.slots.get(i).getItem().copy();
                        itemsCount(targetItemsCount,copy);
                        targetBlockInv.add(copy);
                    }
                    //上面如果不使用copy()在关闭容器后会使第一个元素号变该物品成总数 非常有趣...
//                    System.out.println("???1 "+targetBlockInv.get(0).getCount());
                    client.player.closeContainer();
//                    System.out.println("!!!1 "+targetBlockInv.get(0).getCount());
                    num = 2;
                }
            }
            case 2 -> {
                //打开列表中的容器 只要容器同步列表不为空 就会一直执行此处
                if (client.player == null) return;
                playerItemsCount = new HashMap<>();
                client.gui.setOverlayMessage(Component.literal("剩余 " + syncPosList.size() + " 个容器. 再次按下快捷键取消同步"), false);
                if (!client.player.containerMenu.equals(client.player.inventoryMenu)) return;
                NonNullList<Slot> slots = client.player.inventoryMenu.slots;
                slots.forEach(slot -> itemsCount(playerItemsCount,slot.getItem()));

                if (SYNC_INVENTORY_CHECK.getBooleanValue() && !targetItemsCount.entrySet().stream()
                        .allMatch(target -> playerItemsCount.entrySet().stream()
                                .anyMatch(player ->
                                        ItemStack.isSameItemSameComponents(player.getKey(), target.getKey()) && target.getValue() <= player.getValue()))) return;

                if ((!LitematicaMixinMod.INVENTORY.getBooleanValue() || !openIng) && OpenInventoryPacket.key == null) {
                    for (BlockPos pos : syncPosList) {
                        if (!openInv(pos,true)) continue;
                        closeScreen++;
                        blockPos = pos;
                        num = 3;
                        break;
                    }
                }
                if (syncPosList.isEmpty()) {
                    num = 0;
                    client.gui.setOverlayMessage(Component.literal("同步完成"), false);
                }
            }
            case 3 -> {
                //开始同步 在打开容器后触发
                AbstractContainerMenu sc = client.player.containerMenu;
                if (sc.equals(client.player.inventoryMenu)) return;
                int size = Math.min(targetBlockInv.size(),sc.slots.get(0).container.getContainerSize());

                int times = 0;
                for (int i = 0; i < size; i++) {
                    ItemStack item1 = sc.slots.get(i).getItem();
                    ItemStack item2 = targetBlockInv.get(i).copy();
                    int currNum = item1.getCount();
                    int tarNum = item2.getCount();
                    boolean same = ItemStack.isSameItemSameComponents(item1,item2.copy()) && !item1.isEmpty();
                    if(ItemStack.isSameItemSameComponents(item1,item2) && currNum == tarNum) continue;
                    //不和背包交互
                    if (same) {
                        //有多
                        while (currNum > tarNum) {
                            client.gameMode.handleInventoryMouseClick(sc.containerId, i, 0, ClickType.THROW, client.player);
                            currNum--;
                        }
                    } else {
                        //不同直接扔出
                        client.gameMode.handleInventoryMouseClick(sc.containerId, i, 1, ClickType.THROW, client.player);
                        times++;
                    }
                    boolean thereAreItems = false;
                    //背包交互
                    for (int i1 = size; i1 < sc.slots.size(); i1++) {
                        ItemStack stack = sc.slots.get(i1).getItem();
                        ItemStack currStack = sc.slots.get(i).getItem();
                        currNum = currStack.getCount();
                        boolean same2 = thereAreItems = ItemStack.isSameItemSameComponents(item2,stack);
                        if (same2 && !stack.isEmpty()) {
                            int i2 = stack.getCount();
                            client.gameMode.handleInventoryMouseClick(sc.containerId, i1, 0, ClickType.PICKUP, client.player);
                            for (; currNum < tarNum && i2 > 0; i2--) {
                                client.gameMode.handleInventoryMouseClick(sc.containerId, i, 1, ClickType.PICKUP, client.player);
                                currNum++;
                            }
                            client.gameMode.handleInventoryMouseClick(sc.containerId, i1, 0, ClickType.PICKUP, client.player);
                        }
                        //这里判断没啥用，因为一个游戏刻操作背包太多次.getStack().getCount()获取的数量不准确 下次一定优化，
                        if (currNum != tarNum) times++;
                    }
                    if (!thereAreItems) times++;
                }
                if (times == 0) {
                    syncPosList.remove(blockPos);
                    highlightPosList.remove(blockPos);
                    blockPos = null;
                }
                client.player.closeContainer();
                num = 2;
            }
        }
    }

    public static void tick() {
        tick++;
        tick %= Integer.MAX_VALUE;
        if (num == 2) {
            syncInv();
        }
        addInv();

        if (LitematicaMixinMod.CLOSE_ALL_MODE.getKeybind().isPressed()) {
            LitematicaMixinMod.BEDROCK_SWITCH.setBooleanValue(false);
            LitematicaMixinMod.EXCAVATE.setBooleanValue(false);
            LitematicaMixinMod.REPLACE_BLOCK.setBooleanValue(false);
            LitematicaMixinMod.TOGGLE_PRINTING_MODE.setBooleanValue(false);
            LitematicaMixinMod.PRINTER_MODE.setOptionListValue(State.PrintModeType.PRINTER);
            Printer.currentAction = null;
            client.gui.setOverlayMessage(Component.literal("已关闭全部模式"), false);
        }
        OpenInventoryPacket.tick();
        test();
    }

    static ItemStack itemStack;
    public static void test() {
        if (LitematicaMixinMod.TEST.getKeybind().isPressed()) {
//            QuickShulkerUtils.test();
//            if (itemStack == null) itemStack = client.player.getInventory().getMainHandStack();
//            if (!InventoryUtils.areStacksEqual(client.player.getInventory().getMainHandStack(), itemStack)) {
//                itemStack = client.player.getInventory().getMainHandStack();
//                System.out.println("=======");
//            }
//            OpenInventoryPacket.sendOpenInventory(DataManager.getSelectionManager().getCurrentSelection().getSubRegionBox(DataManager.getSimpleArea().getName()).getPos1(),Minecraft.getInstance().world.getRegistryKey());
        }
    }

    public static void switchPlayerInvToHotbarAir(int slot) {
        if (client.player == null) return;
        LocalPlayer player = client.player;
        AbstractContainerMenu sc = player.containerMenu;
        NonNullList<Slot> slots = sc.slots;
        int i = sc.equals(player.inventoryMenu) ? 9 : 0;
        for (; i < slots.size(); i++) {
            if (slots.get(i).getItem().isEmpty() && slots.get(i).container instanceof Inventory) {
                fi.dy.masa.malilib.util.InventoryUtils.swapSlots(sc, i, slot);
                return;
            }
        }
    }

    public static boolean canInteracted(Vec3 d, double range) {
        IConfigOptionListEntry optionListValue = LitematicaMixinMod.RANGE_MODE.getOptionListValue();
        return optionListValue != State.ListType.SPHERE || canInteracted(range,d);
    }
    public static boolean canInteracted(double range,Vec3 d){
        return client.player != null &&
                d != null &&
                client.player.getEyePosition().distanceToSqr(d) < range * range;
    }

    public static boolean canInteracted(BlockPos blockPos) {
        return blockPos != null && canInteracted(Vec3.atCenterOf(blockPos),getRage());
    }

    public static boolean bedrockCanInteracted(BlockPos blockPos,double range) {
        return client.player != null && client.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockPos)) < range * range;
    }
    public static int getRage(){
        return LitematicaMixinMod.PRINTER_RANGE.getIntegerValue();
    }

    public static int maximumFrameRate = 10;
    public static int frameGenerationTime = getMonitorRefreshRate();
    //根据帧率计算超时时间 尽量不占用帧生成时间 6毫秒预留给打印机处理
    public static int printTimedOut = Math.max(3,frameGenerationTime -6);

    public static int getMonitorRefreshRate() {
        int refreshRate = Math.max(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).refreshRate(),60);
        maximumFrameRate = refreshRate;
        return Math.max(1,1000 / refreshRate);
//        System.out.println("The monitor refresh rate is " + refreshRate);
    }
    public static void exitGameReSet(){
        SwitchItem.reSet();
        Verify.verify = null;
        BreakingFlowController.poslist = new ArrayList<>();
        isRemote = false;
        clientTry = false;
        remoteTime = 0;
    }
    public static Optional<LocalPlayer> getPlayer(){
        return Optional.ofNullable(client.player);
    }

    //刷新物品栏
    public static void refreshPlayerInventory(){
        ClientPacketListener networkHandler = client.getConnection();
        if (getPlayer().isEmpty()) return;
        LocalPlayer player = getPlayer().get();
        if(networkHandler == null) return;
        ItemStack uniqueItem = new ItemStack(Items.STONE);

        // Tags with NaN are not equal, so the server will find an inventory desync and send an inventory refresh to the client
        //#if MC >= 12006
        var nbt = new CompoundTag();
        nbt.putDouble("force_sync", Double.NaN);
        CustomData.set(DataComponents.CUSTOM_DATA, uniqueItem, nbt);
        //#else
        //$$ uniqueItem.getOrCreateTag().putDouble("force_resync", Double.NaN);
        //#endif

        //#if MC >= 12105
        HashedStack itemStackHash = HashedStack.create(uniqueItem, networkHandler.decoratedHashOpsGenenerator());
        //#endif

        networkHandler.send(new ServerboundContainerClickPacket(
                player.containerMenu.containerId,
                player.containerMenu.getStateId(),
                (short) -999, (byte) 2,
                ClickType.QUICK_CRAFT,
                //#if MC < 12105
                //$$ uniqueItem,
                //$$ new Int2ObjectOpenHashMap<>()
                //#else
                new Int2ObjectOpenHashMap<>(),
                itemStackHash
                //#endif


        ));
    }

    public static int getEnchantmentLevel(ItemStack itemStack,
                                          //#if MC > 12006
                                          ResourceKey<Enchantment> enchantment
                                          //#else
                                          //$$ Enchantment enchantment
                                          //#endif
    ){
        //#if MC > 12006
        ItemEnchantments enchantments = itemStack.getEnchantments();

        if (enchantments.equals(ItemEnchantments.EMPTY)) return -1;
        Set<Holder<Enchantment>> enchantmentsEnchantments = enchantments.keySet();
        for (Holder<Enchantment> entry : enchantmentsEnchantments) {
            if (entry.is(enchantment)) {
                return enchantments.getLevel(entry);
            }
        }
        return -1;
        //#else
        //$$ return EnchantmentHelper.getItemEnchantmentLevel(enchantment,itemStack);
        //#endif
    }

    public static void eachBlock(Consumer<Block> consumer){
        for (Block block : BuiltInRegistries.BLOCK) {
            consumer.accept(block);
        }
    }

    public static void eachItem(Consumer<Item> consumer){
        for (Item item : BuiltInRegistries.ITEM) {
            consumer.accept(item);
        }
    }

    //右键单击
//              client.gameMode.handleInventoryMouseClick(sc.containerId, i, 1, ClickType.PICKUP, client.player);
    //左键单击
//              client.gameMode.handleInventoryMouseClick(sc.containerId, i, 0, ClickType.PICKUP, client.player);
    //点击背包外
//              client.gameMode.handleInventoryMouseClick(sc.containerId, -999, 0, ClickType.PICKUP, client.player);
    //丢弃一个
//              client.gameMode.handleInventoryMouseClick(sc.syncId, i, 0, ClickType.THROW, client.player);
    //丢弃全部
//              client.gameMode.handleInventoryMouseClick(sc.syncId, i, 1, ClickType.THROW, client.player);
    //开始拖动
//              client.gameMode.handleInventoryMouseClick(sc.syncId, -999, 0, ClickType.QUICK_CRAFT, client.player);
    //拖动经过的槽
//              client.gameMode.handleInventoryMouseClick(sc.syncId, i1, 1, ClickType.QUICK_CRAFT, client.player);
    //结束拖动
//              client.gameMode.handleInventoryMouseClick(sc.syncId, -999, 2, ClickType.QUICK_CRAFT, client.player);
    //副手交换
//              client.gameMode.handleInventoryMouseClick(sc.syncId, i, 40, ClickType.SWAP, client.player);

}
