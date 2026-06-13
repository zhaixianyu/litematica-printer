package me.aleksilassila.litematica.printer.config;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import me.aleksilassila.litematica.printer.mixin.masa.ButtonGenericAccessor;
import me.aleksilassila.litematica.printer.mixin.masa.IButtonGenericAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;

public class SuperConfig <T extends IConfigBase> implements IConfigBase, IHotkey {
    /*
    * 在配置按钮左侧占用一部分位置创建一个展开按钮
    * 缩减时图标为+展开时图标为-
    * */
    public T mainConfig;
    public LinkedList<IConfigBase> subConfigs = new LinkedList<>();
    public IGuiIcon icon;
    public boolean expand;
    public @NotNull ButtonGeneric expandButton;

    public SuperConfig(T mainConfig, IConfigBase... args) {
        this.mainConfig = mainConfig;
        subConfigs.addAll(Arrays.stream(args).toList());
        icon = MaLiLibIcons.PLUS;
        expand = false;
        expandButton = new ButtonGeneric(0, 0, icon);
    }

    public void switchExpand() {
        expand = !expand;
        icon = expand ? MaLiLibIcons.MINUS : MaLiLibIcons.PLUS;
        ((IButtonGenericAccessor) expandButton).setIcon(icon);
    }

    @Override
    public ConfigType getType() {
        return mainConfig.getType();
    }

    @Override
    public String getName() {
        return mainConfig.getName();
    }

    @Override
    public String getComment() {
        return mainConfig.getComment();
    }

    @Override
    public void setValueFromJsonElement(JsonElement jsonElement) {
        mainConfig.setValueFromJsonElement(jsonElement);
    }

    @Override
    public JsonElement getAsJsonElement() {
        return mainConfig.getAsJsonElement();
    }
    @Override
    public IKeybind getKeybind() {
        if (mainConfig instanceof IHotkey) {
            return ((IHotkey) mainConfig).getKeybind();
        }
        return null;
    }

    //#if MC > 12006
    @Override
    public String getTranslatedName() {
        return mainConfig.getTranslatedName();
    }

    @Override
    public void setPrettyName(String s) {
        mainConfig.setPrettyName(s);
    }

    @Override
    public void setTranslatedName(String s) {
        mainConfig.setTranslatedName(s);
    }

    @Override
    public void setComment(String s) {
        mainConfig.setComment(s);
    }
    //#endif


    //#if MC > 12106
    @Override
    public boolean isDirty() {
        return mainConfig.isDirty();
    }

    @Override
    public void markDirty() {
        mainConfig.markDirty();
    }

    @Override
    public void markClean() {
        mainConfig.markDirty();
    }

    @Override
    public void checkIfClean() {
        mainConfig.checkIfClean();
    }
    //#endif
}
