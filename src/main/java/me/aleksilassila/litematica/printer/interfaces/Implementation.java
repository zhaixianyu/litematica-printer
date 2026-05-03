package me.aleksilassila.litematica.printer.interfaces;

import me.aleksilassila.litematica.printer.mixin.PlayerMoveC2SPacketAccessor;
import me.aleksilassila.litematica.printer.printer.PlacementGuide;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;

/**
 * Dirty class that contains anything and everything that is
 * required to access variables and functions that are inconsistent
 * across different minecraft versions. In other words, this should
 * be the only file that has to be changed in every printer branch.
 */
public class Implementation {
    public static final Item[] HOES = {Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE,
            Items.NETHERITE_HOE, Items.STONE_HOE, Items.WOODEN_HOE};

    public static final Item[] SHOVELS = {Items.DIAMOND_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL,
            Items.NETHERITE_SHOVEL, Items.STONE_SHOVEL, Items.WOODEN_SHOVEL};

    public static final Item[] AXES = {Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE,
            Items.NETHERITE_AXE, Items.STONE_AXE, Items.WOODEN_AXE};

    public static Inventory getInventory(LocalPlayer playerEntity) {
        return playerEntity.getInventory();
    }

    public static Abilities getAbilities(LocalPlayer playerEntity) {
        return playerEntity.getAbilities();
    }

    public static float getYRot(LocalPlayer playerEntity) {
        return playerEntity.getYRot();
    }

    public static float getPitch(LocalPlayer playerEntity) {
        return playerEntity.getXRot();
    }

    public static float[] getRequiredAngles(LocalPlayer player, Direction direction1, Direction direction2) {
        float[] angles = new float[]{player.getYRot(), player.getXRot()};
        if(direction1 == null) return angles;
        if(direction2 == null) {
            angles[0] = Implementation.getRequiredYaw(player, direction1);
            angles[1] = Implementation.getRequiredPitch(player, direction1);
            return angles;
        }
        Direction yaw;
        Direction pitch;
        if (direction2 == Direction.UP || direction2 == Direction.DOWN) {
            yaw = direction1;
            pitch = direction2;
        }else {
            yaw = direction2;
            pitch = direction1;
        }

        angles[0] = Implementation.getRequiredYaw(player, yaw);
        angles[1] = Implementation.getRequiredPitch(player, pitch);
        return angles;
    }
    public static void sendLookPacket(LocalPlayer playerEntity, Direction direction1, Direction direction2) {
        if(direction1 == null) return;
        if(direction2 == null) {
            sendLookPacket(playerEntity, direction1);
            return;
        }
        float[] requiredAngles = getRequiredAngles(playerEntity, direction1, direction2);
        sendLookPacket(playerEntity, requiredAngles[0], requiredAngles[1]);
    }
    public static void sendLookPacket(LocalPlayer playerEntity, Direction playerShouldBeFacing) {
        float requiredYaw = Implementation.getRequiredYaw(playerEntity, playerShouldBeFacing);
        float requiredPitch = Implementation.getRequiredPitch(playerEntity, playerShouldBeFacing);
        sendLookPacket(playerEntity, requiredYaw ,requiredPitch);
    }

    public static void sendLookPacket(LocalPlayer playerEntity, float yaw, float pitch){
        playerEntity.connection.send(new PlayerMoveC2SPacket.LookAndOnGround(
                yaw,
                pitch,
                playerEntity.isOnGround()
                //#if MC > 12101
                ,playerEntity.horizontalCollision
                //#endif
        ));
    }

    public static boolean isLookOnlyPacket(Packet<?> packet) {
        return packet instanceof PlayerMoveC2SPacket.LookAndOnGround;
    }

    public static boolean isLookAndMovePacket(Packet<?> packet) {
        return packet instanceof PlayerMoveC2SPacket.Full;
    }

    public static Packet<?> getFixedLookPacket(LocalPlayer playerEntity, Packet<?> packet, PlacementGuide.Action action) {
        if (action.lookDirection == null) return packet;

        float[] angles = getRequiredAngles(playerEntity, action.lookDirection, action.lookDirection2);

        PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;
        double x = accessor.getX();
        double y = accessor.getY();
        double z = accessor.getZ();
        boolean onGround = accessor.getOnGround();
        return new PlayerMoveC2SPacket.Full(x, y, z, angles[0], angles[1], onGround
                //#if MC > 12101
                ,playerEntity.horizontalCollision
                //#endif
        );
    }

    protected static float getRequiredYaw(LocalPlayer playerEntity, Direction playerShouldBeFacing) {
        if (playerShouldBeFacing.getAxis().isHorizontal()) {
            return playerShouldBeFacing.getPositiveHorizontalDegrees();
        } else {
            return Implementation.getYRot(playerEntity);
        }
    }

    protected static float getRequiredPitch(LocalPlayer playerEntity, Direction playerShouldBeFacing) {
        if (playerShouldBeFacing.getAxis().isVertical()) {
            return playerShouldBeFacing == Direction.DOWN ? 90 : -90;
        } else {
            float pitch = Implementation.getPitch(playerEntity);
            return Math.abs(pitch) < 40 ? pitch : pitch / Math.abs(pitch) * 40;
        }
    }

    public static boolean isInteractive(Block block) {
        for (Class<?> clazz : interactiveBlocks) {
            if (clazz.isInstance(block)) {
                return true;
            }
        }

        return false;
    }

    public enum NewBlocks {
//        LICHEN(AbstractLichenBlock.class),
        ROD(RodBlock.class),
        CANDLES(CandleBlock.class),
        AMETHYST(AmethystClusterBlock.class);

        public final Class<?> clazz;

        NewBlocks(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    public static Class<?>[] interactiveBlocks = {
            ChestBlock.class, AbstractFurnaceBlock.class, CraftingTableBlock.class,
            LeverBlock.class, DoorBlock.class, TrapDoorBlock.class,
            BedBlock.class, RedStoneWireBlock.class, ScaffoldingBlock.class, HopperBlock.class,
            EnchantingTableBlock.class, NoteBlock.class, JukeboxBlock.class, CakeBlock.class,
            FenceGateBlock.class, BrewingStandBlock.class, DragonEggBlock.class, CommandBlock.class,
            BeaconBlock.class, AnvilBlock.class, ComparatorBlock.class, RepeaterBlock.class,
            DropperBlock.class, DispenserBlock.class, ShulkerBoxBlock.class, LecternBlock.class,
            FlowerPotBlock.class, BarrelBlock.class, BellBlock.class, SmithingTableBlock.class,
            LoomBlock.class, CartographyTableBlock.class, GrindstoneBlock.class,
            StonecutterBlock.class,
            //#if MC > 12002
            CrafterBlock.class
            //#endif

    };


}
