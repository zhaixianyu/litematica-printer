package me.aleksilassila.litematica.printer.printer.bedrockUtils;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
//#if MC >= 12105
import java.net.URI;
//#endif

//#if MC > 11802
//#else
//$$
//#endif
public class Messager {
    public static void actionBar(String message){
        Minecraft minecraftClient = Minecraft.getInstance();
        //#if MC > 11802
        MutableComponent translatable = Component.translatable(message);
        //#else
        //$$ Component translatable = new TranslatableComponent(message);
        //#endif
        minecraftClient.gui.setOverlayMessage(translatable,false);
    }


    public static void chat(String message){
        Minecraft minecraftClient = Minecraft.getInstance();
        //#if MC > 11802
        MutableComponent translatable = Component.translatable(message);
        //#else
        //$$ Component translatable = new TranslatableComponent(message);
        //#endif
        minecraftClient.gui.getChat().addClientSystemMessage(translatable);
    }

    public static @NotNull MutableComponent createOpenUrlText(String text, String url) {
        MutableComponent bv = Component.literal(text).copy();
        bv.withStyle(style -> style.withColor(ChatFormatting.GOLD));
        bv.withStyle(style -> style.withUnderlined(true));
        //#if MC >= 12105
        bv.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("点击打开："+url))));
        bv.withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
        //#else
        //$$ bv.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击打开："+url))));
        //$$ bv.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        //#endif
        return bv;
    }
}

