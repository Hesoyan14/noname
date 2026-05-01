package itz.silentcore.feature.module.impl.movement;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import net.minecraft.entity.effect.StatusEffects;

@ModuleAnnotation(name = "AutoSprint", category = Category.MOVEMENT, description = "Автоматический спринт")
public class AutoSprint extends Module {
    
    public static int tickStop = -1;
    
    private final MultiBooleanSetting ignore = new MultiBooleanSetting("Игнорировать",
            MultiBooleanSetting.Value.of("Slowness"),
            MultiBooleanSetting.Value.of("Blindness"));

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        boolean hasSlowness = mc.player.hasStatusEffect(StatusEffects.SLOWNESS);
        boolean hasBlindness = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);

        boolean shouldCancelDueToSlowness = hasSlowness && !ignore.isEnable("Slowness");
        boolean shouldCancelDueToBlindness = hasBlindness && !ignore.isEnable("Blindness");

        boolean horizontal = mc.player.horizontalCollision && !mc.player.collidedSoftly;
        boolean sneaking = mc.player.isSneaking() && !mc.player.isSwimming();

        if (tickStop > 0 || sneaking || shouldCancelDueToSlowness || shouldCancelDueToBlindness) {
            mc.player.setSprinting(false);
        } else if (!horizontal && mc.player.forwardSpeed > 0 && !mc.options.sprintKey.isPressed()) {
            mc.player.setSprinting(true);
        }
        
        tickStop--;
    }
}
