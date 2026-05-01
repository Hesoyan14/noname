package itz.silentcore.mixin.accesors;

import java.util.List;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor
    List<Drawable> getDrawables();

    @Accessor
    List<Element> getChildren();

    @Accessor
    List<Selectable> getSelectables();

    @Invoker("addDrawableChild")
    <T extends Element & Drawable & Selectable> T invokeAddDrawableChild(T drawableElement);
}