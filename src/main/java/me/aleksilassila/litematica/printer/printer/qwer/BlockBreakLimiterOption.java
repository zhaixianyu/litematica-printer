package me.aleksilassila.litematica.printer.printer.qwer;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

import java.util.Locale;

public enum BlockBreakLimiterOption implements IConfigOptionListEntry {
    CONTAIN_NAME,
    CONTAIN_ID,
    EQUAL_NAME,
    TAG_OR_ID,
    REGEX;

    @Override
    public String getStringValue() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getDisplayName() {
        return switch (this) {
            case CONTAIN_NAME -> "方块名称（包含）";
            case CONTAIN_ID -> "方块ID（包含）";
            case EQUAL_NAME -> "方块名称";
            case TAG_OR_ID -> "方块标签或方块ID";
            case REGEX -> "正则表达式";
        };
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        int id = this.ordinal();
        if (forward) {
            if (++id >= values().length) {
                id = 0;
            }
        } else {
            if (--id < 0) {
                id = values().length - 1;
            }
        }
        return values()[id % values().length];
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        try {
            return BlockBreakLimiterOption.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return TAG_OR_ID;
        }
    }
}
