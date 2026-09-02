package me.aleksilassila.litematica.printer.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.aleksilassila.litematica.printer.config.ConfigUi.Tab.*;
import static me.aleksilassila.litematica.printer.config.Configs.addGeneral;
import static me.aleksilassila.litematica.printer.config.Configs.addHotkeys;
import static me.aleksilassila.litematica.printer.config.SuperConfig.superConfigMap;

public class ConfigUi extends GuiConfigsBase {
    private static Tab tab = Tab.ALL;
    public static ConfigUi instance;

    public ConfigUi() {
        super(10, 50, LitematicaMixinMod.MOD_ID, null, "litematica-printer" + FabricLoader.getInstance().getModContainer(LitematicaMixinMod.MOD_ID).get().getMetadata().getVersion());
        instance = this;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;
        for (Tab tab : Tab.values()) {
            x += this.createButton(x, y, -1, tab);
        }

    }

    @Override
    protected void closeGui(boolean showParent) {
        super.closeGui(showParent);
        instance = null;
    }

    private int createButton(int x, int y, int width, Tab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.name);
        button.setEnabled(ConfigUi.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }


    //按钮宽度
//    @Override
//    protected int getConfigWidth()
//    {
//        Tab tab = ConfigUi.tab;
//
//        if (tab == Tab.ALL)
//        {
//            return 120;
//        }
//        else if (tab == Tab.GENERAL)
//        {
//            return 60;-+
//        }
//        return 260;
//    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;
        Tab tab = ConfigUi.tab;
        if (tab == Tab.ALL) {
            configs = Configs.addAllConfigs();
        } else if (tab == GENERAL) {
            configs = addGeneral();
        } else if (tab == PUT) {
            configs = Configs.addPut();
        } else if (tab == EXCAVATE) {
            configs = Configs.addExcavate();
        } else if (tab == HOTKEYS) {
            configs = addHotkeys();
        } else if (tab == COLOR) {
            configs = Configs.addColor();
        } else if (tab == null) {
            return null;
        } else {
            configs = Configs.addAllConfigs();
        }

        superConfigMap.clear();
        List<IConfigBase> processedConfigs = new ArrayList<>();
        for (int i = 0; i < configs.size(); i++) {
            IConfigBase configBase = configs.get(i);
            processedConfigs.add(configBase);
            if (configBase instanceof SuperConfig superConfig && superConfig.expand) {
                processedConfigs.addAll(getSubConfig(superConfig.subConfigs, processedConfigs.size() - 1, superConfig.level));
            }
        }

        return ConfigOptionWrapper.createFor(processedConfigs);
    }

    public List<IConfigBase> getSubConfig(List<IConfigBase> configBases, int index, int level) {
        List<IConfigBase> processedConfigs = new ArrayList<>();
        int levelFix = 10 * (level + 1);
        for (int i = 0; i < configBases.size(); i++) {
            IConfigBase config = configBases.get(i);
            processedConfigs.add(config);
            superConfigMap.put(index + i + 1, levelFix);
            if (config instanceof SuperConfig superConfig && superConfig.expand) {
                List<IConfigBase> subConfigs = getSubConfig(superConfig.subConfigs, index + superConfig.subConfigs.size() + i, superConfig.level);
                processedConfigs.addAll(subConfigs);
                index += subConfigs.size();
            }
        }

        return processedConfigs;
    }

    public static void refresh() {
        if (instance == null) return;
        Objects.requireNonNull(instance.getListWidget()).refreshEntries();
    }

    private static class ButtonListener implements IButtonActionListener {
        private final ConfigUi parent;
        private final Tab tab;

        public ButtonListener(Tab tab, ConfigUi parent) {
            this.tab = tab;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ConfigUi.tab = this.tab;
            this.parent.reCreateListWidget();
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    public enum Tab {
        ALL("全部"),
        GENERAL("通用"),
        PUT("放置"),
        EXCAVATE("挖掘"),
        HOTKEYS("热键"),
        COLOR("颜色"),
        ;

        public final String name;

        Tab(String str) {
            name = str;
        }
    }
}
