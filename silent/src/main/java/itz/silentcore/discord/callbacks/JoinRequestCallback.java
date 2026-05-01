package itz.silentcore.discord.callbacks;

import com.sun.jna.Callback;
import itz.silentcore.discord.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}
