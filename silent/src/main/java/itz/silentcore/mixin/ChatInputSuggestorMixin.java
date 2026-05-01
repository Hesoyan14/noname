package itz.silentcore.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import itz.silentcore.SilentCore;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(value = ChatInputSuggestor.class, priority = 1001)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    @Final
    TextFieldWidget textField;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private boolean completingSuggestions;

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void onRefreshHead(CallbackInfo ci) {
        String text = this.textField.getText();

        if (!text.startsWith(".")) {
            return;
        }

        List<String> suggestions = SilentCore.getInstance().commandManager.getSuggestions(text);

        if (suggestions.isEmpty()) {
            return;
        }

        ci.cancel();

        int cursor = this.textField.getCursor();
        String textBeforeCursor = text.substring(0, Math.min(cursor, text.length()));

        SuggestionsBuilder builder = new SuggestionsBuilder(textBeforeCursor, 0);

        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }

        Suggestions builtSuggestions = builder.build();

        this.pendingSuggestions = CompletableFuture.completedFuture(builtSuggestions);
        this.completingSuggestions = false;
    }
}
