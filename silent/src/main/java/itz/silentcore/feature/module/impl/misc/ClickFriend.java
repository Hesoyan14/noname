package itz.silentcore.feature.module.impl.misc;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.KeyEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.utils.friend.FriendUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

@ModuleAnnotation(name = "ClickFriend", category = Category.MISC, description = "Click on player to add/remove friend")
public class ClickFriend extends Module {
    private int friendKey = GLFW.GLFW_KEY_M;

    @Subscribe
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(friendKey) && 
            mc.crosshairTarget instanceof EntityHitResult result && 
            result.getEntity() instanceof PlayerEntity player) {
            
            if (FriendUtils.isFriend(player)) {
                FriendUtils.removeFriend(player);
            } else {
                FriendUtils.addFriend(player);
            }
        }
    }
}
