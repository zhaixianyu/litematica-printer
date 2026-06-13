package me.aleksilassila.litematica.printer.mixin.masa;


import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ButtonGeneric.class,remap = false)
public abstract class ButtonGenericAccessor implements IButtonGenericAccessor {
    @Shadow(remap = false) @Final
    @Mutable
    @Nullable
    protected IGuiIcon icon;
    @Accessor(value = "icon", remap = false) public abstract void setIcon(IGuiIcon icon);

}
