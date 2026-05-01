package itz.silentcore.feature.module.impl.combat;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.BoundingBoxEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.friend.FriendUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

@ModuleAnnotation(name = "HitBox", category = Category.COMBAT, description = "Expands entity hitboxes")
public class HitBox extends Module implements IMinecraft {
    
    private final NumberSetting xzExpand = new NumberSetting("XZ Expand", 0.2f, 0f, 3f, 0.05f);
    private final NumberSetting yExpand = new NumberSetting("Y Expand", 0f, 0f, 3f, 0.05f);
    
    @Subscribe
    public void onBoundingBox(BoundingBoxEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (living == mc.player) return;
            
            // Skip friends
            if (living instanceof net.minecraft.entity.player.PlayerEntity player && FriendUtils.isFriend(player)) {
                return;
            }
            
            Box box = event.getBox();
            
            float xzExpandValue = xzExpand.getCurrent();
            float yExpandValue = yExpand.getCurrent();
            
            Box changedBox = new Box(
                box.minX - xzExpandValue / 2.0f,
                box.minY - yExpandValue / 2.0f,
                box.minZ - xzExpandValue / 2.0f,
                box.maxX + xzExpandValue / 2.0f,
                box.maxY + yExpandValue / 2.0f,
                box.maxZ + xzExpandValue / 2.0f
            );
            
            event.setBox(changedBox);
        }
    }
}
