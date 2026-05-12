package me.aleksilassila.litematica.printer.printer.zxy.inventory;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;

public class MyPacket {
    private final BlockState blockState;
    private final boolean isOpen;
    public MyPacket(BlockState blockState, boolean isOpen) {
        this.blockState = blockState;
        this.isOpen = isOpen;
    }

    public BlockState getBlockState(){
        return blockState;
    }
    public boolean getIsOpen(){
        return isOpen;
    }
    // 用于序列化数据以发送给客户端的方法
    public static void encode(MyPacket msg, FriendlyByteBuf buffer) {
        buffer.writeVarInt(Block.getId(msg.blockState));
        buffer.writeBoolean(msg.isOpen);
    }

    // 用于接收客户端数据的方法
    public static MyPacket decode(FriendlyByteBuf buffer) {
        return new MyPacket(Block.stateById(buffer.readVarInt()), buffer.readBoolean());
    }
}
