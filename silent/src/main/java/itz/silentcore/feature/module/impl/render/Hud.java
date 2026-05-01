package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.ui.hud.*;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleAnnotation(name = "Hud", category = Category.RENDER, description = "HUD элементы")
public class Hud extends Module {
    private static Hud instance;
    private final List<DragComponent> elements = new ArrayList<>();

    // Настройки для каждого элемента
    public final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public final BooleanSetting hotkeys = new BooleanSetting("Hotkeys", true);
    public final BooleanSetting information = new BooleanSetting("Information", true);
    public final BooleanSetting mediaPlayer = new BooleanSetting("Media Player", true);
    public final BooleanSetting notifications = new BooleanSetting("Notifications", true);
    public final BooleanSetting targetHud = new BooleanSetting("Target HUD", true);
    public final BooleanSetting staffList = new BooleanSetting("Staff List", true);

    public static Hud getInstance() {
        return instance;
    }

    public Hud() {
        super();
        instance = this;
        this.setKey(GLFW.GLFW_KEY_F8);
        setState(true);

        registerElement(new MediaPlayerRenderer());
        registerElement(new NotificationRenderer());
        registerElement(new WatermarkRenderer());
        registerElement(new HotKeysRenderer());
        registerElement(new InformationRenderer());
        registerElement(new TargetHudRenderer());
        registerElement(new StaffListRenderer());
    }

    public void registerElement(DragComponent element) {
        if (!elements.contains(element)) {
            elements.add(element);
        }
    }

    public void unregisterElement(DragComponent element) {
        elements.remove(element);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;

        for (DragComponent element : elements) {
            // Проверяем настройки для каждого элемента
            if (element instanceof WatermarkRenderer && !watermark.isEnabled()) continue;
            if (element instanceof HotKeysRenderer && !hotkeys.isEnabled()) continue;
            if (element instanceof InformationRenderer && !information.isEnabled()) continue;
            if (element instanceof MediaPlayerRenderer && !mediaPlayer.isEnabled()) continue;
            if (element instanceof NotificationRenderer && !notifications.isEnabled()) continue;
            if (element instanceof TargetHudRenderer && !targetHud.isEnabled()) continue;
            if (element instanceof StaffListRenderer && !staffList.isEnabled()) continue;
            
            if (element instanceof MediaPlayerRenderer) {
                ((MediaPlayerRenderer) element).tick();
            }
            element.render(event);
        }

        DragComponent.handleDrag(event, elements);
    }

    @Override
    public JsonObject save() {
        JsonObject base = super.save();
        JsonObject hud = new JsonObject();
        for (DragComponent element : elements) {
            if (!element.isDraggable()) continue;
            JsonObject pos = new JsonObject();
            pos.addProperty("x", element.getX());
            pos.addProperty("y", element.getY());
            hud.add(element.getClass().getSimpleName(), pos);
        }
        base.add("hudElements", hud);
        return base;
    }

    @Override
    public void load(JsonObject object) {
        super.load(object);
        try {
            if (object != null && object.has("hudElements")) {
                JsonObject hud = object.getAsJsonObject("hudElements");
                for (DragComponent element : elements) {
                    if (!element.isDraggable()) continue;
                    String key = element.getClass().getSimpleName();
                    if (hud.has(key)) {
                        JsonObject pos = hud.getAsJsonObject(key);
                        if (pos.has("x")) element.setX(pos.get("x").getAsFloat());
                        if (pos.has("y")) element.setY(pos.get("y").getAsFloat());
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
