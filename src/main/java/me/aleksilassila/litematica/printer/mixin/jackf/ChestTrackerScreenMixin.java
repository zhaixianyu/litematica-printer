package me.aleksilassila.litematica.printer.mixin.jackf;

//#if MC >= 12001
import fi.dy.masa.malilib.util.InventoryUtils;
import me.aleksilassila.litematica.printer.printer.zxy.chesttracker.MemoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.chesttracker.impl.compat.mods.searchables.SearchablesUtil;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.impl.gui.widget.ItemListWidget;
import red.jackf.chesttracker.impl.gui.widget.VerticalScrollWidget;
import red.jackf.chesttracker.impl.util.ItemStacks;

import java.util.*;

//#if MC > 12106
import net.minecraft.client.input.KeyInput;
//#else
//$$
//#endif



@Mixin(value = ChestTrackerScreen.class)
public abstract class ChestTrackerScreenMixin extends Screen {
    @Shadow(remap = false) private ItemListWidget itemList;
    @Shadow(remap = false) private VerticalScrollWidget scroll;
    @Shadow(remap = false) private List<ItemStack> items = Collections.emptyList();

    protected ChestTrackerScreenMixin(Text title) {
        super(title);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private void filter(String filter){
        new Thread(() -> {
            //        SearchablesUtil a;
            //濳影盒等搜索
            List<ItemStack> filtered = new ArrayList<>(items.stream().filter(stack -> {
                return InventoryUtils.getStoredItems(stack, -1).stream().anyMatch((stack2) -> {
                    return ItemStacks.defaultPredicate(stack2,filter);
                });
            }).toList());

            filtered.addAll(SearchablesUtil.ITEM_STACK.filterEntries(this.items, filter.toLowerCase()));
            filtered = filtered.stream().distinct().toList();
            this.itemList.setItems(filtered);
            ChestTrackerConfig.Gui guiConfig = ((ChestTrackerConfig)ChestTrackerConfig.INSTANCE.instance()).gui;
            this.scroll.setDisabled(filtered.size() <= guiConfig.gridWidth * guiConfig.gridHeight);
        }).start();
    }
    @Shadow(remap = false) private Identifier currentMemoryKey;

    @Inject(at = @At("HEAD"), method = "updateItems",remap = false)
    private void upDateItems(CallbackInfo ci) {
        MemoryUtils.currentMemoryKey = currentMemoryKey;
    }

    @Shadow public abstract void close();

//    @Inject(at = @At("HEAD"),method = "keyPressed", cancellable = true)
//    //#if MC >= 12106
//    public void keyPressed1(KeyInput event, CallbackInfoReturnable<Boolean> cir){
//        if (MinecraftClient.getInstance().options.inventoryKey.matchesKey(event) && !(this.getFocused() instanceof TextFieldWidget) ) {
//    //#else
//    //$$ public void keyPressed1(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
//    //$$    if (MinecraftClient.getInstance().options.inventoryKey.matchesKey(keyCode, scanCode) && !(this.getFocused() instanceof TextFieldWidget) ) {
//    //#endif
//            this.close();
//        cir.setReturnValue(true);
//        }
//    }
} 
//#endif