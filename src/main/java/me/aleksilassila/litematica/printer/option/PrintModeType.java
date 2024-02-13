package me.aleksilassila.litematica.printer.option;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public enum PrintModeType implements IConfigOptionListEntry {
    PRINT, BEDROCK, EXCAVATE, FLUID, FARMING, OFF;

    @Override
    public String getStringValue() {
        return switch (this) {
            case PRINT -> "print";
            case BEDROCK -> "bedrock";
            case EXCAVATE -> "excavate";
            case FLUID -> "fluid";
            case FARMING -> "farming";
            case OFF -> "off";
        };
    }

    @Override
    public String getDisplayName() {
        return switch (this) {
            case PRINT -> "自动放置";
            case BEDROCK -> "破基岩";
            case EXCAVATE -> "挖掘";
            case FLUID -> "排流体";
            case FARMING -> "耕作";
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
            case "print" -> PRINT;
            case "bedrock" -> BEDROCK;
            case "excavate" -> EXCAVATE;
            case "fluid" -> FLUID;
            case "farming" -> FARMING;
            default -> OFF;
        };
    }

    @Override
    public String toString() {
        // Debug用的呈现器，代码中不使用
        return this.getDisplayName();
    }
}
