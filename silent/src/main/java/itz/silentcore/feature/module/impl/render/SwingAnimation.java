package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.HandAnimationEvent;
import itz.silentcore.feature.event.impl.SwingDurationEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.module.impl.combat.Aura;
import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@ModuleAnnotation(name = "SwingAnimation", category = Category.RENDER, description = "Custom swing animations")
public class SwingAnimation extends Module implements IMinecraft {
    
    private final ModeSetting swingType = new ModeSetting("Swing Type", "Swipe", 
            "Swipe", "Down", "Smooth", "Smooth 2", "Power", "Feast", "Twist", "Default");
    
    private final NumberSetting hitStrength = new NumberSetting("Hit Strength", 1.0f, 0.5f, 3.0f, 0.1f);
    private final NumberSetting swingSpeed = new NumberSetting("Swing Speed", 1.0f, 0.5f, 4.0f, 0.1f);
    
    private final BooleanSetting onlySwing = new BooleanSetting("Only On Swing", false);
    private final BooleanSetting onlyAura = new BooleanSetting("Only With Aura", false);
    
    @Subscribe
    public void onSwingDuration(SwingDurationEvent event) {
        Aura aura = (Aura) SilentCore.getInstance().moduleManager.getModule("Aura");
        boolean shouldApply = onlyAura.isEnabled() ? 
            (aura != null && aura.isEnabled() && aura.getTarget() != null) : true;
            
        if (shouldApply) {
            event.setAnimation(swingSpeed.getCurrent());
            event.cancel();
        }
    }
    
    @Subscribe
    public void onHandAnimation(HandAnimationEvent event) {
        if (!event.getHand().equals(Hand.MAIN_HAND)) return;
        
        Aura aura = (Aura) SilentCore.getInstance().moduleManager.getModule("Aura");
        boolean shouldApply = onlyAura.isEnabled() ? 
            (aura != null && aura.isEnabled() && aura.getTarget() != null) : true;
            
        if (!shouldApply) return;
        
        if (onlySwing.isEnabled() && mc.player.handSwingTicks == 0) {
            int i = mc.player.getMainArm().equals(Arm.RIGHT) ? 1 : -1;
            event.getMatrices().translate(i * 0.56F, -0.52F, -0.72F);
            event.cancel();
            return;
        }
        
        MatrixStack matrix = event.getMatrices();
        float swingProgress = event.getSwingProgress();
        int i = mc.player.getMainArm().equals(Arm.RIGHT) ? 1 : -1;
        float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float sinSmooth = (float) (Math.sin(swingProgress * Math.PI) * 0.5F);
        float strength = hitStrength.getCurrent();
        
        switch (swingType.get()) {
            case "Twist" -> {
                matrix.translate(i * 0.56F, -0.36F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(80 * i));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -90 * strength));
                matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((sin1 - sin2) * 60 * i * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30));
                matrix.translate(0, -0.1F, 0.05F);
            }
            case "Swipe" -> {
                matrix.translate(0.56F * i, -0.32F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70 * i));
                matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20 * i));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -120 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70));
            }
            case "Default" -> {
                matrix.translate(i * 0.56F, -0.52F - (sin2 * 0.5F * strength), -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 * i));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45 * i));
            }
            case "Down" -> {
                matrix.translate(i * 0.56F, -0.32F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76 * i));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5 * strength));
                matrix.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100));
            }
            case "Smooth" -> {
                matrix.translate(i * 0.56F, -0.42F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + sin1 * -20.0F * strength)));
                matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * sin2 * -20.0F * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
                matrix.translate(0, -0.1, 0);
            }
            case "Smooth 2" -> {
                matrix.translate(i * 0.56F, -0.42F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
                matrix.translate(0, -0.1, 0);
            }
            case "Power" -> {
                matrix.translate(i * 0.56F, -0.32F, -0.72F);
                matrix.translate((-sinSmooth * sinSmooth * sin1) * i * strength, 0, 0);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61 * i));
                matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * strength));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -30 * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60 * strength));
            }
            case "Feast" -> {
                matrix.translate(i * 0.56F, -0.32F, -0.72F);
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75 * i * strength));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -45 * strength));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35 * i));
            }
        }
        
        event.cancel();
    }
}
