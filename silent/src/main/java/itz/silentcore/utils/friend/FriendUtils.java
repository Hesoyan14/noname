package itz.silentcore.utils.friend;

import itz.silentcore.SilentCore;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class FriendUtils {
    private static final Set<String> friends = new HashSet<>();

    public static boolean isFriend(PlayerEntity player) {
        return player != null && friends.contains(player.getName().getString().toLowerCase());
    }

    public static void addFriend(PlayerEntity player) {
        if (player == null) return;
        String name = player.getName().getString().toLowerCase();
        friends.add(name);
        // TODO: Add chat message notification
    }

    public static void removeFriend(PlayerEntity player) {
        if (player == null) return;
        String name = player.getName().getString().toLowerCase();
        friends.remove(name);
        // TODO: Add chat message notification
    }

    public static Set<String> getFriends() {
        return new HashSet<>(friends);
    }
}
