package itz.silentcore.utils.render.web;

import java.lang.reflect.Method;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class McefWebRendererEngine implements WebRendererEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger("xWebRenderer-MCEF");

    private final Variant variant;

    private final Method createBrowserStatic;
    private final boolean hasCinemamodBrowser;
    private final boolean hasCinemamodRenderer;

    private final Object legacyApi;
    private final Method legacyCreateBrowser;
    private final Method legacyTick;

    McefWebRendererEngine() {
        if (!FabricLoader.getInstance().isModLoaded("mcef")) {
            throw new IllegalStateException("MCEF mod is not loaded");
        }

        Optional<Class<?>> modernEntry = ReflectionSupport.findFirstClass("com.cinemamod.mcef.MCEF");
        if (modernEntry.isPresent()) {
            variant = Variant.CINEMAMOD;
            hasCinemamodBrowser = ReflectionSupport.findFirstClass("com.cinemamod.mcef.MCEFBrowser").isPresent();
            hasCinemamodRenderer = ReflectionSupport.findFirstClass("com.cinemamod.mcef.MCEFRenderer").isPresent();
            createBrowserStatic = ReflectionSupport.findMethod(modernEntry.get(), "createBrowser", String.class, boolean.class)
                    .orElseGet(() -> ReflectionSupport.findMethod(modernEntry.get(), "createBrowser", String.class, boolean.class, int.class, int.class)
                            .orElse(null));
            legacyApi = null;
            legacyCreateBrowser = null;
            legacyTick = null;
            if (!isAvailable()) {
                LOGGER.warn("MCEF Cinemamod backend detected but required hooks are missing. Rendering will fall back to placeholder.");
            }
            return;
        }

        Optional<Class<?>> legacyProxy = ReflectionSupport.findFirstClass("net.montoyo.mcef.client.ClientProxy", "net.montoyo.mcef.MCEF");
        if (legacyProxy.isPresent()) {
            variant = Variant.LEGACY;
            Object apiCandidate = null;
            Method createCandidate = null;
            Method tickCandidate = null;
            try {
                Method getInstance = legacyProxy.get().getMethod("getInstance");
                Object proxyInstance = getInstance.invoke(null);
                Method getApi = proxyInstance.getClass().getMethod("getAPI");
                apiCandidate = getApi.invoke(proxyInstance);
                if (apiCandidate != null) {
                    Class<?> apiClass = apiCandidate.getClass();
                    createCandidate = ReflectionSupport.findMethod(apiClass, "createBrowser", String.class, boolean.class).orElse(null);
                    if (createCandidate == null) {
                        createCandidate = ReflectionSupport.findMethodByParams(apiClass, "createBrowser", 1, 4).orElse(null);
                    }
                    tickCandidate = ReflectionSupport.findMethod(apiClass, "onTick", int.class).orElse(null);
                }
            } catch (ReflectiveOperationException ex) {
                LOGGER.warn("Failed to initialise legacy MCEF API", ex);
            }
            legacyApi = apiCandidate;
            legacyCreateBrowser = createCandidate;
            legacyTick = tickCandidate;
            createBrowserStatic = null;
            hasCinemamodBrowser = false;
            hasCinemamodRenderer = false;
            return;
        }

        variant = Variant.NONE;
        createBrowserStatic = null;
        hasCinemamodBrowser = false;
        hasCinemamodRenderer = false;
        legacyApi = null;
        legacyCreateBrowser = null;
        legacyTick = null;
        LOGGER.warn("MCEF mod is loaded but no compatible integration classes were found.");
    }

    @Override
    public boolean isAvailable() {
        return switch (variant) {
            case CINEMAMOD -> createBrowserStatic != null && hasCinemamodBrowser && hasCinemamodRenderer;
            case LEGACY -> legacyApi != null && legacyCreateBrowser != null;
            default -> false;
        };
    }

    @Override
    public WebRendererInstance create(WebRendererConfig config) {
        return switch (variant) {
            case CINEMAMOD -> createCinemamodInstance(config);
            case LEGACY -> {
                LOGGER.warn("Legacy MCEF integration is not supported yet; using placeholder renderer.");
                yield NoOpWebRendererEngine.INSTANCE.create(config);
            }
            default -> NoOpWebRendererEngine.INSTANCE.create(config);
        };
    }

    @Override
    public void tick() {
        if (variant == Variant.LEGACY && legacyApi != null && legacyTick != null) {
            try {
                legacyTick.invoke(legacyApi, 0);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Legacy MCEF tick failed", ex);
            }
        }
    }

    private WebRendererInstance createCinemamodInstance(WebRendererConfig config) {
        if (createBrowserStatic == null) {
            return NoOpWebRendererEngine.INSTANCE.create(config);
        }
        Object browser = null;
        try {
            if (createBrowserStatic.getParameterCount() == 2) {
                browser = createBrowserStatic.invoke(null, config.initialUrl(), config.transparent());
            } else if (createBrowserStatic.getParameterCount() >= 4) {
                browser = createBrowserStatic.invoke(null, config.initialUrl(), config.transparent(), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Failed to create MCEF browser (Cinemamod)", ex);
        }
        if (browser == null) {
            return NoOpWebRendererEngine.INSTANCE.create(config);
        }
        return new CinemamodBrowserInstance(browser, config);
    }

    private enum Variant {
        CINEMAMOD,
        LEGACY,
        NONE
    }

    private static final class CinemamodBrowserInstance implements WebRendererInstance {

        private final Object browser;
        private final WebRendererConfig config;

        private final Method resize;
        private final Method loadUrl;
        private final Method close;
        private final Method focus;
        private final Method mouseMove;
        private final Method mousePress;
        private final Method mouseRelease;
        private final Method mouseWheel;
        private final Method keyPress;
        private final Method keyRelease;
        private final Method keyTyped;

        private final Object renderer;
        private final Method rendererTextureId;

        private int lastWidth = -1;
        private int lastHeight = -1;

        private CinemamodBrowserInstance(Object browser, WebRendererConfig config) {
            this.browser = browser;
            this.config = config;

            Class<?> browserClass = browser.getClass();
            this.resize = ReflectionSupport.findMethod(browserClass, "resize", int.class, int.class).orElse(null);
            this.loadUrl = ReflectionSupport.findMethod(browserClass, "loadURL", String.class)
                    .orElseGet(() -> ReflectionSupport.findMethod(browserClass, "loadUrl", String.class).orElse(null));
            this.close = ReflectionSupport.findMethodByParams(browserClass, "close", 0, 0).orElse(null);
            this.focus = ReflectionSupport.findMethod(browserClass, "setFocus", boolean.class).orElse(null);
            this.mouseMove = ReflectionSupport.findMethod(browserClass, "sendMouseMove", int.class, int.class).orElse(null);
            this.mousePress = ReflectionSupport.findMethod(browserClass, "sendMousePress", int.class, int.class, int.class).orElse(null);
            this.mouseRelease = ReflectionSupport.findMethod(browserClass, "sendMouseRelease", int.class, int.class, int.class).orElse(null);
            this.mouseWheel = ReflectionSupport.findMethod(browserClass, "sendMouseWheel", int.class, int.class, double.class, int.class)
                    .orElse(null);
            this.keyPress = ReflectionSupport.findMethod(browserClass, "sendKeyPress", int.class, long.class, int.class).orElse(null);
            this.keyRelease = ReflectionSupport.findMethod(browserClass, "sendKeyRelease", int.class, long.class, int.class).orElse(null);
            this.keyTyped = ReflectionSupport.findMethod(browserClass, "sendKeyTyped", char.class, int.class).orElse(null);

            Object rendererHandle = null;
            Method textureMethod = null;
            Method getRenderer = ReflectionSupport.findMethodByParams(browserClass, "getRenderer", 0, 0).orElse(null);
            if (getRenderer != null) {
                try {
                    rendererHandle = getRenderer.invoke(browser);
                } catch (ReflectiveOperationException ex) {
                    LOGGER.warn("Unable to fetch MCEF renderer instance", ex);
                }
            }
            if (rendererHandle != null) {
                textureMethod = ReflectionSupport.findMethodByParams(rendererHandle.getClass(), "getTextureID", 0, 0).orElse(null);
                Method initialise = ReflectionSupport.findMethodByParams(rendererHandle.getClass(), "initialize", 0, 0).orElse(null);
                if (initialise != null) {
                    try {
                        initialise.invoke(rendererHandle);
                    } catch (ReflectiveOperationException ex) {
                        LOGGER.debug("MCEF renderer initialise failed", ex);
                    }
                }
            }
            this.renderer = rendererHandle;
            this.rendererTextureId = textureMethod;

            if (this.loadUrl != null) {
                try {
                    this.loadUrl.invoke(browser, config.initialUrl());
                } catch (ReflectiveOperationException ex) {
                    LOGGER.warn("Failed to load initial URL {}", config.initialUrl(), ex);
                }
            }
        }

        @Override
        public void render(DrawContext drawContext, int x, int y, int width, int height, float tickDelta) {
            if (renderer == null || rendererTextureId == null) {
                NoOpWebRendererEngine.INSTANCE.create(config).render(drawContext, x, y, width, height, tickDelta);
                return;
            }

            ensureSize(width, height);

            int textureId = 0;
            try {
                Object texture = rendererTextureId.invoke(renderer);
                if (texture instanceof Number number) {
                    textureId = number.intValue();
                }
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Failed to fetch renderer texture id", ex);
            }
            if (textureId == 0) {
                return;
            }

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, textureId);

            Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix, x, y + height, 0).color(255, 255, 255, 255).texture(0.0F, 1.0F);
            bufferBuilder.vertex(matrix, x + width, y + height, 0).color(255, 255, 255, 255).texture(1.0F, 1.0F);
            bufferBuilder.vertex(matrix, x + width, y, 0).color(255, 255, 255, 255).texture(1.0F, 0.0F);
            bufferBuilder.vertex(matrix, x, y, 0).color(255, 255, 255, 255).texture(0.0F, 0.0F);
            BuiltBuffer builtBuffer = bufferBuilder.end();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }

        @Override
        public void resize(int width, int height) {
            ensureSize(width, height);
        }

        @Override
        public void loadUrl(String url) {
            if (loadUrl == null) {
                return;
            }
            try {
                loadUrl.invoke(browser, url);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Failed to load URL {}", url, ex);
            }
        }

        @Override
        public void mouseMove(double x, double y) {
            if (mouseMove == null) {
                return;
            }
            int sx = scale(x);
            int sy = scale(y);
            try {
                mouseMove.invoke(browser, sx, sy);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Mouse move injection failed", ex);
            }
        }

        @Override
        public void mouseButton(double x, double y, int button, boolean pressed, int modifiers) {
            Method target = pressed ? mousePress : mouseRelease;
            if (target == null) {
                return;
            }
            int sx = scale(x);
            int sy = scale(y);
            try {
                target.invoke(browser, sx, sy, button);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Mouse button injection failed", ex);
            }
        }

        @Override
        public void scroll(double xDelta, double yDelta) {
            if (mouseWheel == null) {
                return;
            }
            int sx = scale(0);
            int sy = scale(0);
            try {
                mouseWheel.invoke(browser, sx, sy, -yDelta, 0);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Scroll injection failed", ex);
            }
        }

        @Override
        public void keyEvent(int keyCode, int scanCode, int modifiers, boolean pressed) {
            Method target = pressed ? keyPress : keyRelease;
            if (target == null) {
                return;
            }
            try {
                target.invoke(browser, keyCode, (long) scanCode, modifiers);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Key event injection failed", ex);
            }
        }

        @Override
        public void charTyped(int codePoint) {
            if (keyTyped == null) {
                return;
            }
            try {
                keyTyped.invoke(browser, (char) codePoint, 0);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Key typed injection failed", ex);
            }
        }

        @Override
        public void setFocused(boolean focused) {
            if (focus == null) {
                return;
            }
            try {
                focus.invoke(browser, focused);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Focus update failed", ex);
            }
        }

        @Override
        public void close() {
            if (close == null) {
                return;
            }
            try {
                close.invoke(browser);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Closing browser failed", ex);
            }
        }

        private void ensureSize(int width, int height) {
            if (resize == null) {
                return;
            }
            if (width <= 0 || height <= 0) {
                return;
            }
            if (width == lastWidth && height == lastHeight) {
                return;
            }
            lastWidth = width;
            lastHeight = height;
            int scaledWidth = scale(width);
            int scaledHeight = scale(height);
            try {
                resize.invoke(browser, scaledWidth, scaledHeight);
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Resize failed", ex);
            }
        }

        private int scale(double value) {
            double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
            return Math.max(1, (int) Math.round(value * scaleFactor));
        }

        private int scale(int value) {
            return scale((double) value);
        }
    }
}
