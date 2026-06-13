package me.aleksilassila.litematica.printer.printer;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import me.aleksilassila.litematica.printer.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.printer.zxy.Utils.Filters;
import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.util.registry.Registry;

public enum State {
    MISSING_BLOCK,
    WRONG_STATE,
    WRONG_BLOCK,
    CORRECT;

    public static State get(BlockState schematicBlockState, BlockState currentBlockState) {
        if (!schematicBlockState.isAir() && (currentBlockState.isAir() ||
                (LitematicaMixinMod.REPLACE.getBooleanValue() &&
                        (LitematicaMixinMod.REPLACEABLE_LIST.getStrings().stream()
                                .anyMatch(string ->
                                        PlacementGuide.Action.isReplaceable(currentBlockState) &&
                                        //!Filters.equalsBlockName(string, schematicBlockState.getBlock()) &&
                                                Filters.equalsBlockName(string, currentBlockState.getBlock()))
                        ))))
            return State.MISSING_BLOCK;
        else if (schematicBlockState.getBlock().equals(currentBlockState.getBlock())
                && !schematicBlockState.equals(currentBlockState))
            return State.WRONG_STATE;
        else if (!schematicBlockState.getBlock().equals(currentBlockState.getBlock()))
            return WRONG_BLOCK;

        return State.CORRECT;
    }


    public static IConfigOptionListEntry cycle(IConfigOptionListEntry current, boolean forward, IConfigOptionListEntry[] values) {
        int id = ((Enum<?>) current).ordinal();

        if (forward) {
            if (++id >= values.length) {
                id = 0;
            }
        } else {
            if (--id < 0) {
                id = values.length - 1;
            }
        }

        return values[id % values.length];
    }

    public static <T extends Enum<T> & IConfigOptionListEntry> T fromString(String name, T[] values, T defaultValue) {
        for (T mode : values) {
            if (mode.getStringValue().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return defaultValue;
    }


    public enum PrintModeType implements IConfigOptionListEntry {
        PRINTER("printer", "打印"),
        BEDROCK("bedrock", "基岩"),
        EXCAVATE("excavate", "挖掘"),
        REPLACE_BLOCK("replace", "替换");

        private final String configString;
        private final String translationKey;

        PrintModeType(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return State.cycle(this, forward, values());
        }

        @Override
        public PrintModeType fromString(String name) {
            return fromStringStatic(name);
        }

        public static PrintModeType fromStringStatic(String name) {
            return State.fromString(name, values(), PRINTER);
        }
    }

    public enum ExcavateListMode implements IConfigOptionListEntry {
        TW("tw", "tw"),
        ME("me", "自带");

        private final String configString;
        private final String translationKey;

        ExcavateListMode(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return State.cycle(this, forward, values());
        }

        @Override
        public ExcavateListMode fromString(String name) {
            return fromStringStatic(name);
        }

        public static ExcavateListMode fromStringStatic(String name) {
            return State.fromString(name, values(), ME);
        }
    }

    public enum ModeType implements IConfigOptionListEntry {
        MULTI("multi", "多模"),
        SINGLE("single", "单模");

        private final String configString;
        private final String translationKey;

        ModeType(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return State.cycle(this, forward, values());
        }

        @Override
        public ModeType fromString(String name) {
            return fromStringStatic(name);
        }

        public static ModeType fromStringStatic(String name) {
            return State.fromString(name, values(), SINGLE);
        }
    }

    public enum ListType implements IConfigOptionListEntry {
        SPHERE("sphere", "球体"),
        CUBE("cube", "立方体");

        private final String configString;
        private final String translationKey;

        ListType(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return State.cycle(this, forward, values());
        }

        @Override
        public ListType fromString(String name) {
            return fromStringStatic(name);
        }

        public static ListType fromStringStatic(String name) {
            return State.fromString(name, values(), SPHERE);
        }
    }

    public enum BreakSchematicBlockType implements IConfigOptionListEntry{
        ALL("all","全部"),
        NOT("not","无"),
        ERROR_BLOCK("errorBlock","仅错误方块"),
        EXCESS_BLOCKS("excessBlock","仅多余方块");

        private final String configString;
        private final String translationKey;

        BreakSchematicBlockType(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return State.cycle(this, forward, values());
        }

        @Override
        public BreakSchematicBlockType fromString(String name) {
            return fromStringStatic(name);
        }

        public static BreakSchematicBlockType fromStringStatic(String name) {
            return State.fromString(name, values(), NOT);
        }

    }

}
