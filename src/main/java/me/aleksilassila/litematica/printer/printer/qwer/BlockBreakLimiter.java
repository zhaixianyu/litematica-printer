package me.aleksilassila.litematica.printer.printer.qwer;

import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import me.aleksilassila.litematica.printer.printer.State;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

import static fi.dy.masa.tweakeroo.config.Configs.Lists.BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST;
import static fi.dy.masa.tweakeroo.config.Configs.Lists.BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST;
import static fi.dy.masa.tweakeroo.tweaks.PlacementTweaks.BLOCK_TYPE_BREAK_RESTRICTION;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.*;

public class BlockBreakLimiter {
    private static final boolean TWEAKEROO_LOADED = FabricLoader.getInstance().isModLoaded("tweakeroo");

    public static boolean breakRestriction(BlockState blockState) {
        if (EXCAVATE_LIMITER.getOptionListValue().equals(State.ExcavateListMode.TWEAKEROO)) {
            return tweakerooBlockBreakLimiter(blockState);
        } else {
            return printerBlockBreakLimiter(blockState);
        }
    }

    private static boolean printerBlockBreakLimiter(BlockState blockState) {
        UsageRestriction.ListType option = (UsageRestriction.ListType) EXCAVATE_LIMIT.getOptionListValue();
        return switch (option) {
            case NONE -> true;
            case BLACKLIST -> EXCAVATE_BLACKLIST.getStrings().stream().noneMatch(match(blockState));
            case WHITELIST -> EXCAVATE_WHITELIST.getStrings().stream().anyMatch(match(blockState));
        };
    }

    private static boolean tweakerooBlockBreakLimiter(BlockState blockState) {
        if (TWEAKEROO_LOADED) {
            UsageRestriction.ListType listType = BLOCK_TYPE_BREAK_RESTRICTION.getListType();
            return switch (listType) {
                case NONE -> true;
                case BLACKLIST ->
                        BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST.getStrings().stream().noneMatch(match(blockState));
                case WHITELIST ->
                        BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST.getStrings().stream().anyMatch(match(blockState));
            };
        }
        return true;
    }

    private static @NotNull Predicate<String> match(BlockState blockState) {
        final BlockBreakLimiterOption option = (BlockBreakLimiterOption) EXCAVATE_LIMIT_OPTION.getOptionListValue();
        String blockId = Registries.BLOCK.getId(blockState.getBlock()).toString();
        String blockName = blockState.getBlock().getName().getString();
        return string -> switch (option) {
            case CONTAIN_NAME -> blockName.contains(string);
            case CONTAIN_ID -> blockId.contains(string);
            case EQUAL_NAME -> Objects.equals(blockName, string);
            case TAG_OR_ID -> blockId.equals(string) || blockState
                    .streamTags()
                    .map(blockTagKey -> "#" + blockTagKey.id())
                    .anyMatch(str -> str.equalsIgnoreCase(string));
            case REGEX -> blockName.matches(string) || blockId.contains(string);
        };
    }
}
