package me.aleksilassila.litematica.printer.mixin.masa.malilibConfig;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WidgetContainer.class)
public interface WidgetContainerAccess {
    @Invoker("addWidget")
    <T extends WidgetBase> T invoker_addWidget(T widget);
}
