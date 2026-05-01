package itz.silentcore.mixin;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.client.WindowStyle;
import itz.silentcore.web.server.WeatherParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kotopushka.compiler.sdk.classes.Profile;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static itz.silentcore.utils.client.IMinecraft.mc;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Unique private long windowHandle = 0;
    @Unique private int currentTitleIndex = 0;
    @Unique private boolean isTypingAnimation = true;
    @Unique private int typingPosition = 0;
    @Unique private boolean isDeleting = false;
    @Unique private String currentDisplayTitle = "";
    @Unique private int pauseCounter = 0;

    @Unique
    private String getBabkaTime() {
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        if (hour >= 5 && hour < 12) {
            return "good morning";
        } else if (hour >= 12 && hour < 18) {
            return "good day";
        } else if (hour >= 18 && hour < 22) {
            return "good evening";
        } else {
            return "good night";
        }
    }

    @Unique
    private String[] getTitles() {
        List<String> titleList = new ArrayList<>(Arrays.asList(
                getBabkaTime() + ", " + Profile.getUsername(),
                "get to ready win with us!",
                "https://t.me/SilenCoreClient",
                "build " + Constants.BUILD,
                "current time " + ClientUtility.getTime(),
                "profile: " + Profile.getUsername() + ", uid: " + Profile.getUid() + ", role: " + Profile.getRole() + ", expire: " + Profile.getExpire()
        ));
        String username = Profile.getUsername();
        switch (username) {
            case "Crashdami":
                titleList.addFirst("НАРКОМАНАМ ВХОД ЗАПРЕЩЁН.");
                break;
            case "sterford":
                titleList.addFirst("ПАСТЕРАМ ВХОД ЗАПРЕЩЁЕН");
                break;
            case "KODEK":
                titleList.addFirst("выйди отсюда тварь");
                break;
        }
        return titleList.toArray(new String[0]);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient$1;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/RunArgs;)V"))
    public void init(RunArgs args, CallbackInfo ci) {
        initializeWindowTitle();
        setWindowIcon();
        if (windowHandle == 0) return;
        WindowStyle.darkmode(windowHandle);
    }

    @Unique
    private void initializeWindowTitle() {
        MinecraftClient client = MinecraftClient.getInstance();
        windowHandle = client.getWindow().getHandle();
        currentDisplayTitle = Constants.PREFIX_TITLE;
    }

    @Unique
    private void setWindowIcon() {
        if (windowHandle == 0) return;
        try {
            InputStream iconStream = MinecraftClient.class.getClassLoader().getResourceAsStream("assets/silentcore/textures/avatar.png");
            if (iconStream != null) {
                byte[] iconData = iconStream.readAllBytes();
                ByteBuffer buffer = MemoryUtil.memAlloc(iconData.length);
                buffer.put(iconData);
                buffer.flip();
                int[] width = new int[1];
                int[] height = new int[1];
                int[] channels = new int[1];
                ByteBuffer image = stbi_load_from_memory(buffer, width, height, channels, 4);
                if (image != null) {
                    GLFWImage.Buffer icons = GLFWImage.malloc(1);
                    GLFWImage icon = icons.get(0);
                    icon.set(width[0], height[0], image);
                    GLFW.glfwSetWindowIcon(windowHandle, icons);
                    MemoryUtil.memFree(image);
                    icons.free();
                }
                MemoryUtil.memFree(buffer);
                iconStream.close();
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        if (windowHandle != 0) {
            setWindowIcon();
            if (windowHandle == 0) return;
            WindowStyle.darkmode(windowHandle);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        TickEvent tickEvent = new TickEvent(true);
        tickEvent.hook();

        update();
    }

    @Inject(at = @At("RETURN"), method = "tick")
    public void tick(CallbackInfo ci) {
        TickEvent tickEvent = new TickEvent(false);
        tickEvent.hook();
    }

    @Unique
    private void update() {
        if (windowHandle == 0) return;
        String[] titles = getTitles();
        String targetTitle = titles[currentTitleIndex];
        if (isTypingAnimation) {
            if (typingPosition < targetTitle.length()) {
                currentDisplayTitle = Constants.PREFIX_TITLE + targetTitle.substring(0, typingPosition + 1);
                typingPosition++;
            } else {
                int pauseDuration = 10;
                if (pauseCounter++ >= pauseDuration) {
                    isDeleting = true;
                    isTypingAnimation = false;
                    pauseCounter = 0;
                }
            }
        } else if (isDeleting) {
            if (typingPosition > 0) {
                currentDisplayTitle = Constants.PREFIX_TITLE + targetTitle.substring(0, typingPosition - 1);
                typingPosition--;
            } else {
                isDeleting = false;
                isTypingAnimation = true;
                currentTitleIndex = (currentTitleIndex + 1) % titles.length;
                typingPosition = 0;
            }
        }

        GLFW.glfwSetWindowTitle(mc.getWindow().getHandle(), silentcore$overrideWindowTitle());
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void aaaaa(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(silentcore$overrideWindowTitle());
    }

    @Unique
    private String silentcore$overrideWindowTitle() {
        return currentDisplayTitle.isEmpty() ? Constants.PREFIX_TITLE : currentDisplayTitle;
    }

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    public void preventDefaultTitle(CallbackInfo ci) {
        ci.cancel();
    }
}