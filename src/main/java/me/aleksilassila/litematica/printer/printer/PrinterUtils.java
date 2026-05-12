package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.interfaces.Implementation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.client;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.remoteItem;

public class PrinterUtils {

	public static Direction[] horizontalDirections = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

	public static boolean playerHasAccessToItem(LocalPlayer playerEntity, Item item) {
		return playerHasAccessToItems(playerEntity, new Item[]{item});
	}

	public static boolean playerHasAccessToItems(LocalPlayer playerEntity, Item[] items) {
		if (items == null || items.length == 0) return true;
		if (Implementation.getAbilities(playerEntity).instabuild) return true;
		else {
            if (!client.player.containerMenu.equals(client.player.inventoryMenu)) return false;
			Inventory inv = Implementation.getInventory(playerEntity);
			for (Item item : items) {
				for (int i = 0; i < inv.getContainerSize(); i++) {
					if (inv.getItem(i).getItem() == item && inv.getItem(i).getCount() > 0) {
                        return true;
                    }
				}
                remoteItem.add(item);
            }
		}
        return false;
	}

    protected static boolean isDoubleSlab(BlockState state) {
    	return state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

	protected static boolean isHalfSlab(BlockState state) {
    	return state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
	}

    public static Direction getHalf(Half half) {
        return half == Half.TOP ? Direction.UP : Direction.DOWN;
    }

    public static Direction axisToDirection(Direction.Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) return direction;
        }
        return Direction.DOWN;
    }

    public static Comparable<?> getPropertyByName(BlockState state, String name) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(name)) {
                return state.getValue(prop);
            }
        }

        return null;
    }

    public static boolean canBeClicked(ClientLevel world, BlockPos pos) {
        return getOutlineShape(world, pos) != Shapes.empty();
    }

    public static VoxelShape getOutlineShape(ClientLevel world, BlockPos pos) {
        return world.getBlockState(pos).getShape(world, pos);
    }

    public static Map<Direction, Vec3> getSlabSides(Level world, BlockPos pos, SlabType requiredHalf) {
        if (requiredHalf == SlabType.DOUBLE) requiredHalf = SlabType.BOTTOM;
        Direction requiredDir = requiredHalf == SlabType.TOP ? Direction.UP : Direction.DOWN;

        Map<Direction, Vec3> sides = new HashMap<>();
        sides.put(requiredDir, new Vec3(0, 0, 0));

        if (world.getBlockState(pos).hasProperty(SlabBlock.TYPE)) {
            sides.put(requiredDir.getOpposite(), Vec3.atLowerCornerOf(requiredDir.getUnitVec3i()).scale(0.5));
        }

        for (Direction side : horizontalDirections) {
            BlockState neighborCurrentState = world.getBlockState(pos.relative(side));

            if (neighborCurrentState.hasProperty(SlabBlock.TYPE) && neighborCurrentState.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                if (neighborCurrentState.getValue(SlabBlock.TYPE) != requiredHalf) {
                    continue;
                }
            }

            sides.put(side, Vec3.atLowerCornerOf(requiredDir.getUnitVec3i()).scale(0.25));
        }

        return sides;
    }
}
