package me.aleksilassila.litematica.printer.config;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;

//#if MC >= 12001
//#else
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryDatabase;
//#endif


public class KeyCallbackHotkeys implements IHotkeyCallback {
    private final Minecraft client;

    public KeyCallbackHotkeys(Minecraft client) {
        this.client = client;
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        if (this.client.player == null || this.client.level == null) return false;

        return false;
    }
}
