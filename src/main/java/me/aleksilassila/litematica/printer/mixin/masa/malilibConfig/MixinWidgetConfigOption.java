package me.aleksilassila.litematica.printer.mixin.masa.malilibConfig;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.*;
import me.aleksilassila.litematica.printer.config.SuperConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WidgetConfigOption.class)
public abstract class MixinWidgetConfigOption {
    @Inject(method = "addConfigOption", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void addConfigOption(int x, int y, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci) {
        if (config instanceof SuperConfig sc) {
            SuperConfig.config = sc;
            SuperConfig.xStart = x + labelWidth + 10;
            ci.cancel();
            addConfigOption(x, y, labelWidth, configWidth, sc.mainConfig);
        }
    }

    @Shadow
    protected abstract void addConfigOption(int x, int y, int labelWidth, int configWidth, IConfigBase config);
}

@Mixin(WidgetContainer.class)
class MixinWidgetContainer {
    @Inject(method = "addWidget", at = @At("HEAD"))
    private <T extends WidgetBase> void addWidget(T widget, CallbackInfoReturnable<T> cir) {
        if (SuperConfig.config != null && SuperConfig.xStart == widget.getX()) {
            SuperConfig.createButton(((WidgetContainer) (Object) this), widget.getX(), widget.getY());
            widget.setX(widget.getX() + 21);
            widget.setWidth(widget.getWidth() - 21);
        }
    }
}

@Mixin(WidgetListConfigOptions.class)
abstract class MixinWidgetListConfigOptions extends WidgetListConfigOptionsBase<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption>{
    @Shadow
    @Final
    protected GuiConfigsBase parent;

    public MixinWidgetListConfigOptions(int x, int y, int width, int height, int configWidth) {
        super(x, y, width, height, configWidth);
    }

    //改变子配置标签宽度
    @Inject(method = "createListEntryWidget(IIIZLfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;)Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;", at = @At("HEAD"), cancellable = true)
    private void createListEntryWidget(int x, int y, int listIndex, boolean isOdd, GuiConfigsBase.ConfigOptionWrapper wrapper, CallbackInfoReturnable<WidgetConfigOption> cir) {
        Integer integer = SuperConfig.superConfigMap.get(listIndex);
        if (integer != null) {
            int newLabelWidth = maxLabelWidth - integer;
            cir.setReturnValue(new WidgetConfigOption(x, y, browserEntryWidth, browserEntryHeight, newLabelWidth, configWidth, wrapper, listIndex, this.parent, this));
        }
    }

    @Shadow
    @Override
    protected abstract WidgetConfigOption createListEntryWidget(int i, int i1, int i2, boolean b, GuiConfigsBase.ConfigOptionWrapper configOptionWrapper);
}