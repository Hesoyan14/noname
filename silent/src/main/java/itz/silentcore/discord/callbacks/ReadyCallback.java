package itz.silentcore.discord.callbacks;

import com.sun.jna.Callback;
import itz.silentcore.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}
