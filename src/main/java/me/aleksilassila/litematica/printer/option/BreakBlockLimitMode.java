package me.aleksilassila.litematica.printer.option;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public enum BreakBlockLimitMode implements IConfigOptionListEntry {
    WHITELIST, BLACKLIST, OFF;

    @Override
    public String getStringValue() {
        return switch (this) {
            case WHITELIST -> "whitelist";
            case BLACKLIST -> "blacklist";
            case OFF -> "off";
        };
    }

    @Override
    public String getDisplayName() {
        return switch (this) {
            case WHITELIST -> "白名单";
            case BLACKLIST -> "黑名单";
            case OFF -> "关闭";
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
        return switch (value.toLowerCase()) {
            case "whitelist" -> WHITELIST;
            case "blacklist" -> BLACKLIST;
            default -> OFF;
        };
    }
}
