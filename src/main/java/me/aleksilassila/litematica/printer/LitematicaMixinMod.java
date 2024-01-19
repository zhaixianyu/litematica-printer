package me.aleksilassila.litematica.printer;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import me.aleksilassila.litematica.printer.option.BreakBlockLimitMode;
import me.aleksilassila.litematica.printer.option.KeyCallbackPrintMode;
import me.aleksilassila.litematica.printer.option.PrintModeType;
import me.aleksilassila.litematica.printer.printer.zxy.OpenInventoryPacket;
import net.fabricmc.api.ModInitializer;

import java.util.List;

public class LitematicaMixinMod implements ModInitializer {
    public static final ConfigInteger PRINT_INTERVAL = new ConfigInteger("printInterval", 4, 0, 20, "Print interval in game ticks. Lower values mean faster printing speed.\nIf the printer creates \"ghost blocks\", raise this value.");
    public static final ConfigInteger PRINTING_RANGE = new ConfigInteger("printingRange", 5, 1, 20, "Printing block place range\nLower values are recommended for servers.");
    //    public static final ConfigBoolean PRINT_WATER    = new ConfigBoolean("printWater",    false, "Whether or not the printer should place water\n source blocks or make blocks waterlogged.");
    public static final ConfigBoolean PRINT_IN_AIR = new ConfigBoolean("printInAir", true, "Whether or not the printer should place blocks without anything to build on.\nBe aware that some anti-cheat plugins might notice this.");
    public static final ConfigBoolean PRINT_SWITCH = new ConfigBoolean("printingSwitch", false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    public static final ConfigBoolean REPLACE_FLUIDS = new ConfigBoolean("replaceFluids", false, "Whether or not fluid source blocks should be replaced by the printer.");
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", false, "Whether or not the printer should use normal logs if stripped\nversions are not available and then strip them with an axe.");
    public static boolean shouldPrintInAir = PRINT_IN_AIR.getBooleanValue();
    public static boolean shouldReplaceFluids = REPLACE_FLUIDS.getBooleanValue();
    public static final ConfigOptionList PRINT_MODE = new ConfigOptionList("打印模式", PrintModeType.PRINT, """
            自动放置:根据原理图自动放置方块
            破基岩:啊吧啊吧
            挖掘:挖掘所选区的方块
            排流体:在岩浆源、水源处放沙子
            耕作:自动收割并种植农作物，同时对未成熟的农作物使用骨粉
            """);
    public static final ConfigBoolean PRINT_WATER_LOGGED_BLOCK = new ConfigBoolean("打印含水方块", false, "启用后会自动放置并破坏冰来使方块含水，但是要确保可以瞬间破坏冰");
    public static final ConfigBoolean PRINT_LIMIT = new ConfigBoolean("打印受渲染层限制", false, "自动放置以外的模式是否也受渲染层限制");
    public static final ConfigStringList FLUID_BLOCK_LIST = new ConfigStringList("排流体方块名单", ImmutableList.of("minecraft:sand"), "");
    public static final ConfigBoolean SKIP = new ConfigBoolean("是否放置侦测器和红石块", true, "关闭后会跳过侦测器和红石块的放置");
    public static final ConfigBoolean QUICKSHULKER = new ConfigBoolean("快捷潜影盒", false, "在有快捷潜影盒mod的情况下可以直接从背包内的潜影盒取出物品\n替换的位置为投影的预设位置,如果所有预设位置都有濳影盒则不会替换。");
    public static final ConfigBoolean INVENTORY = new ConfigBoolean("远程交互容器", false, "在服务器有远程交互容器mod的情况下可以远程交互\n替换的位置为投影的预设位置。");
    public static final ConfigStringList INVENTORY_LIST = new ConfigStringList("库存白名单", ImmutableList.of("minecraft:chest"), "");
    public static final ConfigOptionList BREAK_BLOCK_LIMIT_MODE = new ConfigOptionList("方块破坏限制模式", BreakBlockLimitMode.OFF, "启用后挖掘模式只能破坏指定的方块");
    public static final ConfigStringList BREAK_BLOCK_WHITELIST = new ConfigStringList("方块破坏白名单列表", ImmutableList.of(""), "只有此列表中的方块才能被挖掘模式破坏");
    public static final ConfigStringList BREAK_BLOCK_BLACKLIST = new ConfigStringList("方块破坏黑名单列表", ImmutableList.of(""), "此列表中的方块不能被挖掘模式破坏");

    public static ImmutableList<IConfigBase> getConfigList() {
        List<IConfigBase> list = new java.util.ArrayList<>(Configs.Generic.OPTIONS);
        list.add(PRINT_SWITCH);
        list.add(PRINT_INTERVAL);
        list.add(PRINTING_RANGE);
        list.add(PRINT_IN_AIR);
        list.add(REPLACE_FLUIDS);
        list.add(STRIP_LOGS);
        list.add(PRINT_MODE);
        list.add(FLUID_BLOCK_LIST);
        list.add(INVENTORY_LIST);
        // 打印含水方块
        list.add(PRINT_WATER_LOGGED_BLOCK);
        // 打印受渲染层限制
        list.add(PRINT_LIMIT);
        // 方块破坏限制模式
        list.add(BREAK_BLOCK_LIMIT_MODE);
        list.add(BREAK_BLOCK_WHITELIST);
        list.add(BREAK_BLOCK_BLACKLIST);
        list.add(0, SKIP);
        list.add(0, QUICKSHULKER);
        list.add(0, INVENTORY);

        return ImmutableList.copyOf(list);
    }

    // Hotkeys
    public static final ConfigHotkey PRINT = new ConfigHotkey("print", "V", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "Prints while pressed");
    public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("togglePrintingMode", "CAPS_LOCK", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY, "Allows quickly toggling on/off Printing mode");

    public static final ConfigHotkey BEDROCK_MODE = new ConfigHotkey("破基岩模式", "", "切换为破基岩模式 此模式下 y轴会从上往下判定.");
    public static final ConfigHotkey EXE_MODE = new ConfigHotkey("挖掘模式", "K", "挖掘所选区的方块");
    public static final ConfigHotkey SYNC_INVENTORY = new ConfigHotkey("容器同步", "Y", "对准你想要达成的容器方块。按下热键将开始同步投影所选区域内的所选容器");
    public static final ConfigHotkey PRINTER_INVENTORY = new ConfigHotkey("打印机库存", "G", "如果远程取物的目标是未加载的区块将会增加取物品的时间，用投影选区后按下热键\n" +
            "打印机工作时将会优先使用该库存内的物品\n" +
            "建议库存区域内放置假人来常加载区块");
    public static final ConfigHotkey REVISION_PRINT = new ConfigHotkey("清空打印机库存", "C,G", "清空打印机库存");

    public static List<ConfigHotkey> getHotkeyList() {
        List<ConfigHotkey> list = new java.util.ArrayList<>(Hotkeys.HOTKEY_LIST);
        list.add(PRINT);
        list.add(TOGGLE_PRINTING_MODE);
        list.add(BEDROCK_MODE);
        list.add(EXE_MODE);
        list.add(PRINTER_INVENTORY);
        list.add(SYNC_INVENTORY);
        list.add(REVISION_PRINT);

        return ImmutableList.copyOf(list);
    }

    @Override
    public void onInitialize() {
        OpenInventoryPacket.registerReceivePacket();
        TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(PRINT_SWITCH));
        // 破基岩
        BEDROCK_MODE.getKeybind().setCallback(new KeyCallbackPrintMode(PRINT_MODE, PrintModeType.BEDROCK));
        // 挖掘
        EXE_MODE.getKeybind().setCallback(new KeyCallbackPrintMode(PRINT_MODE, PrintModeType.EXCAVATE));
    }
}