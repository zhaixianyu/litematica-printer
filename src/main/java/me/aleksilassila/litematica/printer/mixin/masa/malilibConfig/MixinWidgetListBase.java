package me.aleksilassila.litematica.printer.mixin.masa.malilibConfig;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.malilib.gui.GuiScrollBar;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import me.aleksilassila.litematica.printer.config.SuperConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


@Mixin(WidgetListBase.class)
public abstract class MixinWidgetListBase<TYPE, WIDGET extends WidgetListEntryBase<TYPE>> {
    @Shadow
    protected final List<TYPE> listContents = new ArrayList<>();
    @Shadow
    protected final List<WIDGET> listWidgets = new ArrayList<>();
    @Shadow
    protected final GuiScrollBar scrollBar = new GuiScrollBar();
    @Shadow
    protected int maxVisibleBrowserEntries;

    @WrapOperation(method = "reCreateListEntryWidgets", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/widgets/WidgetListBase;createListEntryWidgetIfSpace(IIIII)Lfi/dy/masa/malilib/gui/widgets/WidgetListEntryBase;"))
    private WIDGET onReCreateListEntryWidgets(WidgetListBase instance, int x, int y, int listIndex, int usableHeight, int usedHeight, Operation<WIDGET> original) {
        this.listContents.clear();
        Collection<TYPE> entries = this.getAllEntries();
        if (this.hasFilter()) {
            this.addFilteredContents(entries);
        } else {
            this.addNonFilteredContents(entries);
        }

        if (this.getShouldSortList()) {
            this.listContents.sort(this.getComparator());
        }


        for (int i = scrollBar.getValue(); i < listContents.size(); i++) {
            Integer i1 = SuperConfig.superConfigMap.get(i);
            int fixX = i1 == null ? x : x + i1;
            WIDGET widget = createListEntryWidgetIfSpace(fixX, y, i, usableHeight, usedHeight);
            if (widget == null) continue;
            listWidgets.add(widget);
            maxVisibleBrowserEntries++;
            usedHeight += widget.getHeight();
            y += widget.getHeight();
        }
        return null;
    }

    @Shadow
    protected abstract void refreshBrowserEntries();

    @Shadow
    protected abstract boolean hasFilter();

    @Shadow
    protected abstract void addFilteredContents(Collection<TYPE> entries);

    @Shadow
    protected abstract void addNonFilteredContents(Collection<TYPE> placements);

    @Shadow
    protected abstract boolean getShouldSortList();

    @Shadow
    protected abstract Comparator<TYPE> getComparator();

    @Shadow
    protected abstract Collection<TYPE> getAllEntries();

    @Shadow
    protected abstract WIDGET createListEntryWidgetIfSpace(int x, int y, int listIndex, int usableHeight, int usedHeight);
}
