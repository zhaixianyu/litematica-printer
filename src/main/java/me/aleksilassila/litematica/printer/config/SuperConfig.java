package me.aleksilassila.litematica.printer.config;

import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.widgets.WidgetContainer;
import me.aleksilassila.litematica.printer.interfaces.IButtonGenericAccessor;
import me.aleksilassila.litematica.printer.mixin.masa.malilibConfig.WidgetContainerAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class SuperConfig extends ConfigBoolean {
    /*
     * 在配置按钮左侧占用一部分位置创建一个展开按钮
     * 缩减时图标为+展开时图标为-
     * 找到创建按钮方式
     * */
    public IConfigBase mainConfig;
    public LinkedList<IConfigBase> subConfigs = new LinkedList<>();
    public IGuiIcon icon;
    public boolean expand;
    public @NotNull ButtonGeneric expandButton;
    public int level;

    public static SuperConfig config;
    public static int xStart = 0;
    //记录配置列表中的索引值
    public static LinkedHashMap<Integer, Integer> superConfigMap = new LinkedHashMap<>();

    public SuperConfig(IConfigBase mainConfig, IConfigBase... args) {
        super("", false
        //#if MC <= 12006
                , ""
        //#endif
        );
        this.mainConfig = mainConfig;
        subConfigs.addAll(Arrays.stream(args).toList());
        subConfigs.forEach(iConfigBase -> {
            if (iConfigBase instanceof SuperConfig sc) sc.level = this.level + 1;
        });
        icon = MaLiLibIcons.PLUS;
        expand = false;
    }

    public void switchExpand() {
        expand = !expand;
        icon = expand ? MaLiLibIcons.MINUS : MaLiLibIcons.PLUS;
        ((IButtonGenericAccessor) expandButton).setIcon(icon);
        ConfigUi.refresh();
    }

    public static void createButton(WidgetContainer widgetContainer, int x, int y) {
        SuperConfig copyConfig = SuperConfig.config;
        SuperConfig.config = null;
        copyConfig.expandButton = new ButtonGeneric(x, y, 20, 20, null, copyConfig.icon);
        copyConfig.expandButton.setActionListener((buttonGeneric, i) -> copyConfig.switchExpand());
        ((WidgetContainerAccess) widgetContainer).invoker_addWidget(copyConfig.expandButton);
    }
}
