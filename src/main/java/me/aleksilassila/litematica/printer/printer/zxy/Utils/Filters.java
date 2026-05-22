package me.aleksilassila.litematica.printer.printer.zxy.Utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.getItemName;

public class Filters {
    //包含/比较
    public static boolean filters(String str1,String str2,String[] argument){
        ArrayList<String> strArr = new ArrayList<>(Arrays.asList(argument));
        AtomicBoolean b = new AtomicBoolean(str1.equals(str2));
        strArr.forEach(str -> {
            if (str.equals("c")) {
                b.set(str1.contains(str2));
            }
        });
        return b.get();
    }
    public static boolean equalsBlockName(String blockName, BlockState blockState, BlockPos pos){
        return equalsBlockName(blockName,blockState.getBlock());
    }
    public static boolean equalsBlockName(String blockName, Block block){
        return equalsName(blockName,block);
    }
    public static boolean equalsItemName(String itemName, ItemStack itemStack){
        return equalsName(itemName,itemStack);
    }

    public static boolean equalsName(String blockName, Object o){
        if(blockName.equals("all")) return true;
        if(blockName.startsWith("//")) return false;
        Block block = null;
        ItemStack itemStack = null;

        if(o instanceof Block ob){
            block = ob;
        }else if(o instanceof ItemStack oi){
            itemStack = oi;
        }else return false;

        String originId = block != null ?
                BuiltInRegistries.BLOCK.getKey(block).toString() : BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        String[] args = blockName.split(",");
        String oName = args[0];
        if(args.length > 1){
            args = Arrays.copyOfRange(args,1,args.length);
        }else args = new String[]{};
        boolean b = Arrays.stream(args).noneMatch("!"::equals);
        if (Filters.filters(originId,oName,args)) return b;
        try {
           return block != null ? getTag(block.builtInRegistryHolder().tags(),blockName,args) : getTag(itemStack.getTags(),blockName,args);
        }catch (Exception ignored){}

        //中文 、 拼音
        String name = block != null ?  block.getName().getString() : getItemName(itemStack);
        if (Filters.filters(name,oName,args)) return b;

        ArrayList<String> pinYin = PinYinSearch.getPinYin(name);
        String[] finalStrs1 = args;
        if (pinYin.stream().anyMatch(p -> Filters.filters(p,oName, finalStrs1))) return b;
        return !b;
    }

    private static<T> boolean getTag(Stream<TagKey<T>> t, String name, String[] tags) throws NoTag {
        //标签
        if (name.length() > 1 && name.charAt(0) == '#') {
            AtomicBoolean theLabelIsTheSame = new AtomicBoolean(false);
            String fix1 = name.split("#")[1];
            t.forEach(tag -> {
                String tagName = tag.location().toString();
                if (Filters.filters(tagName,fix1, tags)) {
                    theLabelIsTheSame.set(true);
                }
            });
            return theLabelIsTheSame.get();
        }else {
            throw new NoTag();
        }
    }
}
class NoTag extends Exception {

}