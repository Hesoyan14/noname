package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.*;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.utils.interact.PlayerInteractionHelper;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@ModuleAnnotation(name = "FreeLook", category = Category.RENDER, description = "Look around without moving your body")
public class FreeLook extends Module {
    private Perspective perspective;
    private Turns angle;
    private int freeLookKey = GLFW.GLFW_KEY_LEFT_ALT;

    @Subscribe
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(freeLookKey)) {
            perspective = mc.options.getPerspective();
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
        }
    }

    @Subscribe
    public void onFov(FovEvent e) {
        if (PlayerInteractionHelper.isKeyPressed(freeLookKey)) {
            if (mc.options.getPerspective().isFirstPerson()) {
                mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            }
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
        } else if (perspective != null) {
            mc.options.setPerspective(perspective);
            perspective = null;
            angle = null;
        }
    }

    @Subscribe
    public void onMouseRotation(MouseRotationEvent e) {
        if (PlayerInteractionHelper.isKeyPressed(freeLookKey)) {
            if (angle == null) {
                angle = MathAngle.cameraAngle();
            }
            angle.setYaw(angle.getYaw() + (float) e.getCursorDeltaX() * 0.15F);
            angle.setPitch(MathHelper.clamp(angle.getPitch() + (float) e.getCursorDeltaY() * 0.15F, -90F, 90F));
            e.cancel();
        } else {
            angle = null;
        }
    }

    @Subscribe
    public void onCamera(CameraEvent e) {
        if (PlayerInteractionHelper.isKeyPressed(freeLookKey) && angle != null) {
            e.setAngle(angle);
            e.cancel();
        }
    }
}
