package me.aleksilassila.litematica.printer.option;

import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.InfoUtils;

public class KeyCallbackPrintMode implements IHotkeyCallback {
    private final ConfigOptionList optionList;
    private final PrintModeType printModeType;

    public KeyCallbackPrintMode(ConfigOptionList optionList, PrintModeType printModeType) {
        this.optionList = optionList;
        this.printModeType = printModeType;
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        // 如果当前模式已经是要切换的模式，就关闭该模式
        boolean onOrOff = optionList.getOptionListValue().equals(printModeType);
        optionList.setOptionListValue(onOrOff ? PrintModeType.OFF : printModeType);
        // onOrOff为true：关闭，false：打开
        InfoUtils.printActionbarMessage(onOrOff ? PrintModeType.OFF.getDisplayName() : printModeType.getDisplayName());
        return true;
    }
}
