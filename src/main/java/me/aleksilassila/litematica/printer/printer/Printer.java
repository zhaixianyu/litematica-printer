package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.interfaces.IClientPlayerInteractionManager;
import me.aleksilassila.litematica.printer.interfaces.Implementation;
import me.aleksilassila.litematica.printer.option.BreakBlockLimitMode;
import me.aleksilassila.litematica.printer.option.PrintModeType;
import me.aleksilassila.litematica.printer.printer.memory.Memory;
import me.aleksilassila.litematica.printer.printer.memory.MemoryDatabase;
import me.aleksilassila.litematica.printer.printer.utils.BreakingFlowController;
import me.aleksilassila.litematica.printer.printer.zxy.OpenInventoryPacket;
import me.aleksilassila.litematica.printer.printer.zxy.Verify;
import net.minecraft.block.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

import static fi.dy.masa.litematica.selection.SelectionMode.NORMAL;
import static me.aleksilassila.litematica.printer.printer.Printer.TempData.max;
import static me.aleksilassila.litematica.printer.printer.Printer.TempData.min;
import static me.aleksilassila.litematica.printer.printer.zxy.OpenInventoryPacket.openIng;

public class Printer extends PrinterUtils {
    public static boolean up = true;

    public class TempData {
        public static int[] min;
        public static int[] max;

        public static boolean xuanQuFanWeiNei(BlockPos pos) {
            AreaSelection i = DataManager.getSelectionManager().getCurrentSelection();
            if (i == null) return false;
            if (DataManager.getSelectionManager().getSelectionMode() == NORMAL) {
                boolean fw = false;
                List<Box> arr = i.getAllSubRegionBoxes();
                for (Box box : arr) {
                    if (comparePos(box, pos)) {
                        return true;
                    }
                }
                return fw;
            } else {
                Box box = i.getSubRegionBox(DataManager.getSimpleArea().getName());
                return comparePos(box, pos);
            }
        }

        // 判断指定坐标是否超出原理图渲染范围
        private static boolean outOfRenderRange(BlockPos blockPos) {
            if (LitematicaMixinMod.PRINT_LIMIT.getBooleanValue()) {
                return !DataManager.getRenderLayerRange().isPositionWithinRange(blockPos);
            }
            return false;
        }

        static boolean comparePos(Box box, BlockPos pos) {
            int x = 0, y = 0, z = 0;
            if (pos != null) {
                x = pos.getX();
                y = pos.getY();
                z = pos.getZ();
            }
            if (box == null) return false;
            BlockPos kpos1 = box.getPos1();
            BlockPos kpos2 = box.getPos2();
            min = new int[]{
                    kpos1.getX() < kpos2.getX() ? kpos1.getX() : kpos2.getX(),
                    kpos1.getY() < kpos2.getY() ? kpos1.getY() : kpos2.getY(),
                    kpos1.getZ() < kpos2.getZ() ? kpos1.getZ() : kpos2.getZ()
            };
            max = new int[]{
                    kpos1.getX() > kpos2.getX() ? kpos1.getX() : kpos2.getX(),
                    kpos1.getY() > kpos2.getY() ? kpos1.getY() : kpos2.getY(),
                    kpos1.getZ() > kpos2.getZ() ? kpos1.getZ() : kpos2.getZ()
            };
            if (
                    x < min[0] || x > max[0] ||
                            y < min[1] || y > max[1] ||
                            z < min[2] || z > max[2]
            ) {
                return false;
            } else {
                return true;
            }
        }

        public ClientPlayerEntity player;
        public ClientWorld world;
        public WorldSchematic worldSchematic;

        public TempData(ClientPlayerEntity player, ClientWorld world, WorldSchematic worldSchematic) {
            this.player = player;
            this.world = world;
            this.worldSchematic = worldSchematic;
        }
    }

    private static Printer INSTANCE;
    @NotNull
    private final MinecraftClient client;
    public final PlacementGuide guide;
    public final Queue queue;

    int tick = 0;

    public static void init(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        INSTANCE = new Printer(client);

    }

    public static @Nullable Printer getPrinter() {
//        if (INSTANCE == null) {
//            INSTANCE = new Printer(client);
//        }

        return INSTANCE;
    }

    private Printer(MinecraftClient client) {
        this.client = client;

        this.guide = new PlacementGuide(client);
        this.queue = new Queue(this);

        INSTANCE = this;
    }

    /*
    Fixme legit mode:
        - scaffoldings
    Fixme other:
        - signs
        - rotating blocks (signs, skulls)
     */
    void fluidMode(TempData data) {
        int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();

        for (int y = range; y > -range - 1; y--) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    BlockPos pos = data.player.getBlockPos().north(x).west(z).up(y);
                    BlockState currentState = data.world.getBlockState(pos);
                    if (!TempData.xuanQuFanWeiNei(pos)) continue;
                    if (TempData.outOfRenderRange(pos)) {
                        continue;
                    }
                    if (currentState.getFluidState().isOf(Fluids.LAVA) || currentState.getFluidState().isOf(Fluids.WATER)) {
                        blocklist = LitematicaMixinMod.FLUID_BLOCK_LIST.getStrings();
                        for (int i = 0; i < blocklist.size(); i++) {
                            try {
                                Item item = Registries.ITEM.get(new Identifier(blocklist.get(i)));
                                fluidList.add(item);
                            } catch (Exception e) {
                            }
                        }
                        switchToItems(data.player, fluidList.toArray(new Item[fluidList.size()]));
                        Item item = Implementation.getInventory(data.player).getMainHandStack().getItem();
                        String itemid = Registries.ITEM.getId(item).toString();
                        if (!blocklist.stream().anyMatch(b -> itemid.contains(b) || item.getName().toString().contains(b))) {
                            items2.addAll(fluidList);
                            return;
                        }
//                        sendClick(pos, Vec3d.ofCenter(pos));
                        ((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(pos, Direction.UP, Vec3d.ofCenter(pos));
                        if (tickRate == 0) {
                            continue;
                        }
                        return;
                    }
                }
            }
        }
    }

    // 挖掘模式
    @SuppressWarnings("DataFlowIssue")
    void miningMode(TempData data) {
        for (int y = range; y > -range - 1; y--) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    BlockPos pos = data.player.getBlockPos().north(x).west(z).up(y);
                    if (TempData.outOfRenderRange(pos)) {
                        continue;
                    }
                    if (canBreakBlock(client.world.getBlockState(pos)) && TempData.xuanQuFanWeiNei(pos) && waJue(pos)) {
                        return;
                    }
                }
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean waJue(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        BlockState currentState = world.getBlockState(pos);
//                    if (requiredState.isOf(Blocks.GLASS) && !currentState.isOf(Blocks.AIR) && !currentState.isOf(Blocks.BEDROCK)) {
        if (
                !currentState.isAir() &&
                        !currentState.isOf(Blocks.AIR) &&
                        !currentState.isOf(Blocks.CAVE_AIR) &&
                        !currentState.isOf(Blocks.VOID_AIR) &&
                        currentState.getBlock().getHardness() != -1 &&
                        !(currentState.getBlock() instanceof FluidBlock) &&
                        !client.player.isBlockBreakingRestricted(client.world, pos, client.interactionManager.getCurrentGameMode())
        ) {
            client.interactionManager.updateBlockBreakingProgress(pos, Direction.DOWN);
            client.interactionManager.cancelBlockBreaking();
            return !world.getBlockState(pos).isOf(Blocks.AIR);
        }
        return false;
    }

    // 是否可以挖掘方块
    private static boolean canBreakBlock(BlockState blockState) {
        if (LitematicaMixinMod.BREAK_BLOCK_LIMIT_MODE.getOptionListValue() instanceof BreakBlockLimitMode breakBlockLimitMode) {
            return switch (breakBlockLimitMode) {
                case WHITELIST ->
                        checkBreakBlockLimitList(blockState, LitematicaMixinMod.BREAK_BLOCK_WHITELIST.getStrings());
                case BLACKLIST ->
                        !checkBreakBlockLimitList(blockState, LitematicaMixinMod.BREAK_BLOCK_BLACKLIST.getStrings());
                default -> true;
            };
        }
        return true;
    }

    // 检查白名单
    private static boolean checkBreakBlockLimitList(BlockState blockState, List<String> whiteList) {
        for (String blockName : whiteList) {
            if (blockName.startsWith("#")) {
                List<String> list = blockState.streamTags().map(blockTagKey -> "#" + blockTagKey.id()).toList();
                for (String str : list) {
                    if (Objects.equals(str, blockName)) {
                        return true;
                    }
                }
            }
            try {
                if (blockState.isOf(Registries.BLOCK.get(new Identifier(blockName)))) {
                    return true;
                }
            } catch (InvalidIdentifierException ignored) {
            }
        }
        return false;
    }

    // 破基岩
    public void jymod(TempData data) {
        int range = 3;
        BreakingFlowController.tick();
        int maxy = -9999;
        for (int y = range; y > -range - 1; y--) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    BlockPos pos = data.player.getBlockPos().north(x).west(z).up(y);
                    if (TempData.outOfRenderRange(pos)) {
                        continue;
                    }
                    BlockState currentState = data.world.getBlockState(pos);
                    if (currentState.isOf(Blocks.PISTON) && !data.world.getBlockState(pos.down()).isOf(Blocks.BEDROCK)) {
                        BreakingFlowController.poslist.add(pos);
                    } else if (currentState.isOf(Blocks.PISTON_HEAD)) {
                        switchToItems(client.player, new Item[]{Items.AIR, Items.DIAMOND_PICKAXE});
                        ((IClientPlayerInteractionManager) client.interactionManager)
                                .rightClickBlock(pos, Direction.UP, Vec3d.ofCenter(pos));
                    }

                    if (TempData.xuanQuFanWeiNei(pos) && currentState.isOf(Blocks.BEDROCK) && pos.down().isWithinDistance(data.player.getPos(), 4f) && !client.world.getBlockState(pos.up()).isOf(Blocks.BEDROCK)) {
                        if (maxy == -9999) maxy = y;
                        if (y < maxy) return;
                        BreakingFlowController.addBlockPosToList(pos);
                    }
                }
            }
        }
    }

    public boolean verify() {
        if (client.isInSingleplayer() && client.isRealmsEnabled()) return true;
        String address = null;
        try {
            address = client.getCurrentServerEntry().address.split(":")[0];
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        if (Verify.getVerify() == null) {
            new Verify(address, client.player);
            return false;
        } else {
            return Verify.getVerify().tick(address);
        }
    }

    public int range;
    int tickRate;
    boolean isFacing = false;
    Item[] item2 = null;
    List<String> blocklist;
    public static HashSet<Item> items2 = new HashSet<>();
    public static HashSet<Item> fluidList = new HashSet<>();
    static LinkedList<TempPos> tempList = new LinkedList<>();

    class TempPos {
        public TempPos(BlockPos pos, int tick) {
            this.pos = pos;
            this.tick = tick;
        }

        public BlockPos pos;
        public int tick;
    }

    public void tick() {
        if (!verify()) {
            return;
        }
        TempData data = new TempData(client.player, client.world, SchematicWorldHandler.getSchematicWorld());
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        ClientPlayerEntity pEntity = client.player;
        ClientWorld world = client.world;

        tickRate = LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue();
        range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();
        tick = tick == 0x7fffffff ? 0 : tick + 1;

        if (tickRate != 0) {
            if (tick % tickRate != 0) {
                return;
            }
            queue.sendQueue(data.player);
        }

        if (isFacing) {
            switchToItems(pEntity, item2);
            queue.sendQueue(data.player);
            isFacing = false;
        }

        if (!items2.isEmpty() && !isOpenHandler && !openIng) {
            if (LitematicaMixinMod.QUICKSHULKER.getBooleanValue() && openShulker(items2)) {
                return;
            } else if (LitematicaMixinMod.INVENTORY.getBooleanValue()) {
                for (Item item : items2) {
                    MemoryDatabase database = MemoryDatabase.getCurrent();
                    if (database != null) {
                        for (Identifier dimension : database.getDimensions()) {
                            for (Memory memory : database.findItems(item.getDefaultStack(), dimension)) {
                                OpenInventoryPacket.sendOpenInventory(memory.getPosition(), RegistryKey.of(RegistryKeys.WORLD, dimension));
                                isOpenHandler = true;
                                return;
                            }
                        }
                    }
                }
                items2 = new HashSet<>();
                isOpenHandler = false;
            }
        }
        if (isOpenHandler) {
            return;
        }
        // 开始打印
        if (LitematicaMixinMod.PRINT_MODE.getOptionListValue() instanceof PrintModeType printMode) {
            switch (printMode) {
                case BEDROCK -> jymod(data);
                case EXCAVATE -> miningMode(data);
                case FLUID -> fluidMode(data);
                case FARMING -> farming(data);
                case PRINT -> autoPlace(pEntity, worldSchematic, world);
                default -> {
                    // 什么也不做
                }
            }
        }
    }

    // 自动放置
    private void autoPlace(ClientPlayerEntity pEntity, WorldSchematic worldSchematic, ClientWorld world) {
        for (TempPos tempPos : tempList) {
            tempPos.tick++;
        }
        LitematicaMixinMod.shouldPrintInAir = LitematicaMixinMod.PRINT_IN_AIR.getBooleanValue();
        LitematicaMixinMod.shouldReplaceFluids = LitematicaMixinMod.REPLACE_FLUIDS.getBooleanValue();
        // forEachBlockInRadius:
        for (int y = -range; y < range + 1; y++) {
            for (int x = -range; x < range + 1; x++) {
                z:
                for (int z = -range; z < range + 1; z++) {
                    BlockPos pos = pEntity.getBlockPos().north(x).west(z).up(y);
                    BlockState requiredState = worldSchematic.getBlockState(pos);
                    // 挖掉之前未成功破坏的冰
                    if (LitematicaMixinMod.PRINT_WATER_LOGGED_BLOCK.getBooleanValue() && !requiredState.isOf(Blocks.ICE)
                            && pEntity.getWorld().getBlockState(pos).isOf(Blocks.ICE) && canWaterLogged(requiredState)) {
                        // 判断的后半部分是必须的
                        if (waJue(pos) || !pEntity.getWorld().getBlockState(pos).isOf(Blocks.WATER)) {
                            return;
                        }
                    }
                    PlacementGuide.Action action = guide.getAction(world, worldSchematic, pos);
                    if (requiredState.isOf(Blocks.NETHER_PORTAL) ||
                            requiredState.isOf(Blocks.END_PORTAL)
                    ) continue;
                    //跳过侦测器和红石块的放置
                    if ((requiredState.isOf(Blocks.OBSERVER) || requiredState.isOf(Blocks.REDSTONE_BLOCK)) && !LitematicaMixinMod.SKIP.getBooleanValue()) {
                        continue;
                    }

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(pos)) {
                        continue;
                    }
                    if (action == null) continue;

                    Direction side = action.getValidSide(world, pos);
                    if (side == null) continue;

                    Item[] requiredItems = action.getRequiredItems(requiredState.getBlock());
                    if (playerHasAccessToItems(pEntity, requiredItems)) {
                        // Handle shift and chest placement
                        // Won't be required if clickAction
                        boolean useShift = false;
                        if (requiredState.contains(ChestBlock.CHEST_TYPE)) {
                            // Left neighbor from player's perspective
                            BlockPos leftNeighbor = pos.offset(requiredState.get(ChestBlock.FACING).rotateYClockwise());
                            BlockState leftState = world.getBlockState(leftNeighbor);

                            switch (requiredState.get(ChestBlock.CHEST_TYPE)) {
                                case SINGLE:
                                case RIGHT: {
                                    useShift = true;
                                    break;
                                }
                                case LEFT: { // Actually right
                                    if (leftState.contains(ChestBlock.CHEST_TYPE) && leftState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
                                        useShift = false;

                                        // Check if it is possible to place without shift
                                        if (Implementation.isInteractive(world.getBlockState(pos.offset(side)).getBlock())) {
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                    break;
                                }
                            }
                        } else if (Implementation.isInteractive(world.getBlockState(pos.offset(side)).getBlock())) {
                            useShift = true;
                        }

                        Direction lookDir = action.getLookDirection();

                        if ((requiredState.isOf(Blocks.PISTON) ||
                                requiredState.isOf(Blocks.STICKY_PISTON) ||
                                requiredState.isOf(Blocks.OBSERVER) ||
                                requiredState.isOf(Blocks.DROPPER) ||
                                requiredState.isOf(Blocks.DISPENSER)) && isFacing
                        ) {
                            continue;
                        }
                        // 打印含水方块
                        if (printWaterLoggedBlock(requiredState, pEntity, lookDir, pos)) {
                            return;
                        }
                        //发送放置准备
                        sendPlacementPreparation(pEntity, requiredItems, lookDir);
                        action.queueAction(queue, pos, side, useShift, lookDir != null);
                        if (requiredState.isOf(Blocks.NOTE_BLOCK)) {
                            queue.sendQueue(pEntity);
                            continue;
                        }
                        if (tickRate == 0) {
                            //处理不能快速放置的方块
                            if (requiredState.isOf(Blocks.PISTON) ||
                                    requiredState.isOf(Blocks.STICKY_PISTON) ||
                                    requiredState.isOf(Blocks.OBSERVER) ||
                                    requiredState.isOf(Blocks.DROPPER) ||
                                    requiredState.isOf(Blocks.DISPENSER)
                            ) {
                                item2 = requiredItems;
                                isFacing = true;
                                continue;
                            }
                            for (int i = 0; i < tempList.size(); i++) {
                                if (tempList.get(i).tick > 3) {
                                    tempList.remove(i);
                                    i--;
                                    continue;
                                }
                                if (this.queue.target.equals(tempList.get(i).pos) && tempList.get(i).tick < 3) {
                                    this.queue.clearQueue();
                                    continue z;
                                }

                            }
                            tempList.add(new TempPos(this.queue.target, 0));
                            queue.sendQueue(pEntity);
                            continue;
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * 打印原理图中的含水方块，在放置原理图方块前先放置一块冰并打破来使方块含水
     *
     * @param requiredState 原理图中需要放置的方块
     * @param pEntity       要放置方块的客户端玩家实体
     * @param lookDir       干啥用的，不知道啊
     * @param pos           要在世界放置方块的位置
     * @return 是否需要取消放置
     */
    private boolean printWaterLoggedBlock(BlockState requiredState, ClientPlayerEntity pEntity, Direction lookDir, BlockPos pos) {
        // 放置含水方块
        if (LitematicaMixinMod.PRINT_WATER_LOGGED_BLOCK.getBooleanValue()
                && canWaterLogged(requiredState)) {
            // 如果要放置方块的位置已经有水了，并且是水源，直接取消放置冰
            BlockState blockState = pEntity.getWorld().getBlockState(pos);
            if (blockState.isOf(Blocks.WATER) && blockState.get(FluidBlock.LEVEL) == 0) {
                return false;
            }
            // 如果玩家处于创造模式，取消放置
            if (pEntity.isCreative()) {
                pEntity.sendMessage(Text.literal("需要处于生存模式"), true);
                return true;
            }
            // 从背包中拿取冰
            switchToItems(pEntity, new Item[]{Items.ICE});
            // 如果背包中没有冰，取消放置
            if (!pEntity.getMainHandStack().isOf(Items.ICE)) {
                pEntity.sendMessage(Text.literal("你物品栏里可能没有冰"), true);
                return true;
            }
            // 看向冰方块
            sendLook(pEntity, lookDir);
            // 如果要放置方块的位置已经是冰了，不再放置冰
            if (!pEntity.getWorld().getBlockState(pos).isOf(Blocks.ICE)) {
                // 右键点击
                queue.rightClickBlock(pEntity, pos);
            }
            // 从物品栏拿取镐子
            switchToItems(pEntity, new Item[]{Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE});
            // 获取玩家主手上的物品
            ItemStack mainHandStack = pEntity.getMainHandStack();
            // 检查镐子是否不带精准采集
            if ((mainHandStack.isOf(Items.DIAMOND_PICKAXE) || mainHandStack.isOf(Items.NETHERITE_PICKAXE))
                    // 镐子不能带有精准采集附魔
                    && !EnchantmentHelper.hasSilkTouch(mainHandStack)) {
                // 破坏冰
                // 如果冰没有成功破坏，waJue()返回true，取消放置
                return waJue(pos);
            } else {
                // 如果背包中没有合适的镐子，取消放置
                pEntity.sendMessage(Text.literal("需要一把不带精准采集的钻石镐或下界合金镐"), true);
                return true;
            }
        }
        return false;
    }

    // 判断方块是否含水
    private static boolean canWaterLogged(BlockState requiredState) {
        try {
            return requiredState.get(BooleanProperty.of("waterlogged"));
        } catch (Throwable e) {
            // 这样写应该没问题吧
            return false;
        }
    }

    // 耕作
    private void farming(TempData data) {
        for (int y = range; y > -range - 1; y--) {
            for (int x = -range; x < range + 1; x++) {
                for (int z = -range; z < range + 1; z++) {
                    ClientPlayerEntity player = data.player;
                    World world = player.getWorld();
                    BlockPos pos = player.getBlockPos().north(x).west(z).up(y);
                    BlockState blockState = world.getBlockState(pos);
                    if (TempData.outOfRenderRange(pos)) {
                        continue;
                    }
                    if (TempData.xuanQuFanWeiNei(pos)) {
                        if (blockState.isOf(Blocks.AIR) || blockState.isOf(Blocks.CAVE_AIR)) {
                            if (world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                                switchToItems(player, new Item[]{Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS});
                                ItemStack itemStack = player.getMainHandStack();
                                if (itemStack.isOf(Items.WHEAT_SEEDS)
                                        || itemStack.isOf(Items.POTATO)
                                        || itemStack.isOf(Items.CARROT)
                                        || itemStack.isOf(Items.BEETROOT_SEEDS)) {
                                    this.queue.rightClickBlock(player, pos);
                                }
                            }
                        }
                        if (blockState.getBlock() instanceof CropBlock cropBlock) {
                            if (blockState.isOf(Blocks.TORCHFLOWER_CROP)) {
                                continue;
                            }

                            if (blockState.get(CropBlock.AGE) == cropBlock.getMaxAge()) {
                                waJue(pos);
                                if (blockState.isOf(Blocks.WHEAT)) {
                                    switchToItems(player, new Item[]{Items.WHEAT_SEEDS});
                                } else if (blockState.isOf(Blocks.POTATOES)) {
                                    switchToItems(player, new Item[]{Items.POTATO});
                                } else if (blockState.isOf(Blocks.CARROTS)) {
                                    switchToItems(player, new Item[]{Items.CARROT});
                                } else if (blockState.isOf(Blocks.BEETROOTS)) {
                                    switchToItems(player, new Item[]{Items.BEETROOT_SEEDS});
                                }
                            } else {
                                switchToItems(player, new Item[]{Items.BONE_MEAL});
                            }
                            ItemStack itemStack = player.getMainHandStack();
                            if (itemStack.isOf(Items.WHEAT_SEEDS)
                                    || itemStack.isOf(Items.POTATO)
                                    || itemStack.isOf(Items.CARROT)
                                    || itemStack.isOf(Items.BEETROOT_SEEDS)
                                    || itemStack.isOf(Items.BONE_MEAL)) {
                                this.queue.rightClickBlock(player, pos);
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<BlockPos> siftBlock(String blockName) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        AreaSelection i = DataManager.getSelectionManager().getCurrentSelection();
        List<Box> box;
        if (i == null) return blocks;
        box = i.getAllSubRegionBoxes();
        for (int index = 0; index < box.size(); index++) {
            TempData.comparePos(box.get(index), null);
            for (int x = min[0]; x <= max[0]; x++) {
                for (int y = min[1]; y <= max[1]; y++) {
                    for (int z = min[2]; z <= max[2]; z++) {
                        BlockPos pos = new BlockPos(new BlockPos(x, y, z));
                        BlockState state = null;
                        if (client.world != null) {
                            state = client.world.getBlockState(pos);
                        }
                        Block block = state.getBlock();
                        if (Registries.BLOCK.getId(block).toString().contains(blockName)) {
                            blocks.add(pos);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    private void sendPlacementPreparation(ClientPlayerEntity player, Item[] requiredItems, Direction lookDir) {
        switchToItems(player, requiredItems);
        sendLook(player, lookDir);
    }

    public static boolean isOpenHandler = false;
    public static OpenScreenS2CPacket packet = null;

    public void switchInv() {
//        if(true) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler sc = player.currentScreenHandler;
        if (sc.equals(player.playerScreenHandler)) {
            return;
        }
        for (Item item : items2) {
//            System.out.println(Arrays.toString(items2));
            for (int y = 0; y < sc.slots.size(); y++) {
                if (sc.slots.get(y).getStack().getItem().equals(item)) {
                    String[] str = Configs.Generic.PICK_BLOCKABLE_SLOTS.getStringValue().split(",");
                    if (str.length == 0) return;
                    for (String s : str) {
                        if (s == null) break;
                        try {
                            int c = Integer.parseInt(s) - 1;
                            if (Registries.ITEM.getId(player.getInventory().getStack(c).getItem()).toString().contains("shulker_box") && LitematicaMixinMod.QUICKSHULKER.getBooleanValue()) {
                                MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("无可替换的槽位，请将预选位的濳影盒换个位置"), false);
                                continue;
                            }
//                            System.out.println(y);
//                            System.out.println(c);
                            fi.dy.masa.malilib.util.InventoryUtils.swapSlots(sc, y, c);
                            player.getInventory().selectedSlot = c;
                            player.closeHandledScreen();
                            isOpenHandler = false;
                            items2 = new HashSet<>();
                            return;
                        } catch (Exception e) {
                            System.out.println("切换物品异常");
                        }
                    }
                }
            }
        }
        items2 = new HashSet<>();
        isOpenHandler = false;
        player.closeHandledScreen();

    }

    boolean openShulker(HashSet<Item> items) {
        for (Item item : items) {
            ScreenHandler sc = MinecraftClient.getInstance().player.playerScreenHandler;
//            if(!MinecraftClient.getInstance().player.currentScreenHandler.equals(sc))return false;
            for (int i = 0; i < sc.slots.size(); i++) {
                ItemStack stack = sc.slots.get(i).getStack();
                if (i < 9) {
                    int i2 = i + 36;
                    stack = sc.slots.get(i2).getStack();
                }
                Item item2 = sc.slots.get(i).getStack().getItem();
                String itemid = Registries.ITEM.getId(item2).toString();
                if (itemid.contains("shulker_box")) {
                    DefaultedList<ItemStack> items1 = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(stack, -1);
                    if (items1.stream().anyMatch(s1 -> s1.getItem().equals(item))) {
                        try {
                            Class quickShulker = Class.forName("net.kyrptonaught.quickshulker.client.ClientUtil");
                            Method checkAndSend = quickShulker.getDeclaredMethod("CheckAndSend", ItemStack.class, int.class);
                            if (i < 9) i += 36;
                            checkAndSend.invoke(checkAndSend, stack, i);
                            isOpenHandler = true;
                            //                                    System.out.println("open "+b);
                            return true;
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        return false;
    }

    public void switchToItems(ClientPlayerEntity player, Item[] items) {
        if (items == null) return;
        PlayerInventory inv = Implementation.getInventory(player);
        for (Item item : items) {
            if (inv.getMainHandStack().getItem() == item) {
                return;
            }
        }
        for (Item item : items) {
            if (Implementation.getAbilities(player).creativeMode) {
                InventoryUtils.setPickedItemToHand(new ItemStack(item), client);
                client.interactionManager.clickCreativeStack(client.player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
                return;
            } else {
                int slot = -1;
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStack(i).getItem() == item && inv.getStack(i).getCount() > 0)
                        slot = i;
                }
                if (slot != -1) {
                    swapHandWithSlot(player, slot);
                    return;
                }
            }
        }
    }

    public void swapHandWithSlot(ClientPlayerEntity player, int slot) {
        ItemStack stack = Implementation.getInventory(player).getStack(slot);
        InventoryUtils.setPickedItemToHand(stack, client);
    }

    public void sendLook(ClientPlayerEntity player, Direction direction) {
        if (direction != null) {
            Implementation.sendLookPacket(player, direction);
        }
        queue.lookDir = direction;
    }

    public static class Queue {
        public BlockPos target;
        public Direction side;
        public Vec3d hitModifier;
        public boolean shift = false;
        public boolean didSendLook = true;

        public Direction lookDir = null;

        final Printer printerInstance;

        public Queue(Printer printerInstance) {
            this.printerInstance = printerInstance;
        }

        public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3d hitModifier) {
            queueClick(target, side, hitModifier, true, true);
        }

        public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3d hitModifier, boolean shift, boolean didSendLook) {
            if (LitematicaMixinMod.PRINT_INTERVAL.getIntegerValue() != 0) {
                if (this.target != null) {
                    System.out.println("Was not ready yet.");
                    return;
                }
            }

            this.didSendLook = didSendLook;
            this.target = target;
            this.side = side;
            this.hitModifier = hitModifier;
            this.shift = shift;

        }

        public void sendQueue(ClientPlayerEntity player) {
            if (target == null || side == null || hitModifier == null) return;

            boolean wasSneaking = player.isSneaking();

            Direction direction = side.getAxis() == Direction.Axis.Y ?
                    ((lookDir == null || !lookDir.getAxis().isHorizontal())
                            ? Direction.NORTH : lookDir) : side;

            hitModifier = new Vec3d(hitModifier.z, hitModifier.y, hitModifier.x);
            hitModifier = hitModifier.rotateY((direction.asRotation() + 90) % 360);

            Vec3d hitVec = Vec3d.ofCenter(target)
                    .add(Vec3d.of(side.getVector()).multiply(0.5))
                    .add(hitModifier.multiply(0.5));

            if (shift && !wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            else if (!shift && wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            ((IClientPlayerInteractionManager) printerInstance.client.interactionManager)
                    .rightClickBlock(target, side, hitVec);

//            System.out.println("Printed at " + (target.toString()) + ", " + side + ", modifier: " + hitVec);

            if (shift && !wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            else if (!shift && wasSneaking)
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

            clearQueue();
        }

        // 潜行右键单击
        private void rightClickBlock(ClientPlayerEntity player, BlockPos pos) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            printerInstance.client.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, true));
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        public void clearQueue() {
            this.target = null;
            this.side = null;
            this.hitModifier = null;
            this.lookDir = null;
            this.shift = false;
            this.didSendLook = true;
        }
    }
}