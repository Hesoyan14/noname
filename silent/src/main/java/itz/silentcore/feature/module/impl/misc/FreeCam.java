package itz.silentcore.feature.module.impl.misc;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.*;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.math.Calculate;
import itz.silentcore.utils.simulate.Simulations;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Vec3d;

@ModuleAnnotation(name = "FreeCam", category = Category.MISC, description = "Free camera movement")
public class FreeCam extends Module {
    private final NumberSetting speedSetting = new NumberSetting("Speed", 2.0f, 0.5f, 5.0f, 0.1f, "Camera speed");
    private final BooleanSetting freezeSetting = new BooleanSetting("Freeze", "Freeze player in place", false);
    
    public Vec3d pos, prevPos;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            prevPos = pos = new Vec3d(mc.getEntityRenderDispatcher().camera.getPos().toVector3f());
        }
        super.onEnable();
    }

    @Subscribe
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket && freezeSetting.isEnabled()) {
            e.cancel();
        } else if (e.getPacket() instanceof PlayerRespawnS2CPacket || e.getPacket() instanceof GameJoinS2CPacket) {
            setState(false);
        }
    }

    @Subscribe
    public void onMove(MoveEvent e) {
        if (freezeSetting.isEnabled()) {
            e.setMovement(Vec3d.ZERO);
        }
    }

    @Subscribe
    public void onInput(InputEvent e) {
        float speed = speedSetting.getCurrent();
        double[] motion = Simulations.calculateDirection(e.forward(), e.sideways(), speed);

        prevPos = pos;
        // Use mc.options to check jump/sneak keys instead of Input fields
        boolean jumping = mc.options.jumpKey.isPressed();
        boolean sneaking = mc.options.sneakKey.isPressed();
        pos = pos.add(motion[0], jumping ? speed : sneaking ? -speed : 0, motion[1]);

        e.inputNone();
    }

    @Subscribe
    public void onCameraPosition(CameraPositionEvent e) {
        if (pos != null && prevPos != null) {
            e.setPos(Calculate.interpolate(prevPos, pos));
        }
        mc.options.setPerspective(Perspective.FIRST_PERSON);
    }
}
