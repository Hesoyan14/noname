package itz.silentcore.utils.render.web.screen;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import itz.silentcore.utils.render.web.WebRendererConfig;
import itz.silentcore.utils.render.web.WebRendererInstance;
import itz.silentcore.utils.render.web.WebRendererManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class HelloWorldWebRendererScreen extends Screen {

    private static final int PADDING = 12;
    private static final String HELLO_WORLD_HTML = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <title>Liquid Glass</title>
              <style>
                body {
                  margin: 0;
                  height: 100vh;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background: radial-gradient(circle at top, #243b55, #141e30);
                  font-family: "Segoe UI", sans-serif;
                  color: #e8f1ff;
                  overflow: hidden;
                }
                .message {
                  position: absolute;
                  top: 40px;
                  font-size: 22px;
                  letter-spacing: 0.08em;
                  text-transform: uppercase;
                  opacity: 0.85;
                }
                .hint {
                  position: absolute;
                  bottom: 40px;
                  font-size: 14px;
                  opacity: 0.65;
                }
              </style>
            </head>
            <body>
              <div class="message">Liquid Glass Demo</div>
              <div class="hint">Drag the orb around – rendered via MCEF</div>
              <script>
              // Vanilla JS Liquid Glass Effect - adapted for MCEF demo
              (function() {
                'use strict';
                
                if (window.liquidGlass) {
                  window.liquidGlass.destroy();
                  console.log('Previous liquid glass effect removed.');
                }
                
                function smoothStep(a, b, t) {
                  t = Math.max(0, Math.min(1, (t - a) / (b - a)));
                  return t * t * (3 - 2 * t);
                }

                function length(x, y) {
                  return Math.sqrt(x * x + y * y);
                }

                function roundedRectSDF(x, y, width, height, radius) {
                  const qx = Math.abs(x) - width + radius;
                  const qy = Math.abs(y) - height + radius;
                  return Math.min(Math.max(qx, qy), 0) + length(Math.max(qx, 0), Math.max(qy, 0)) - radius;
                }

                function texture(x, y) {
                  return { type: 't', x, y };
                }

                function generateId() {
                  return 'liquid-glass-' + Math.random().toString(36).substr(2, 9);
                }

                class Shader {
                  constructor(options = {}) {
                    this.width = options.width || 300;
                    this.height = options.height || 200;
                    this.fragment = options.fragment || ((uv) => texture(uv.x, uv.y));
                    this.canvasDPI = 1;
                    this.id = generateId();
                    this.offset = 10;
                    
                    this.mouse = { x: 0, y: 0 };
                    this.mouseUsed = false;
                    
                    this.createElement();
                    this.setupEventListeners();
                    this.updateShader();
                  }

                  createElement() {
                    this.container = document.createElement('div');
                    this.container.style.cssText = `
                      position: fixed;
                      top: 50%;
                      left: 50%;
                      transform: translate(-50%, -50%);
                      width: ${this.width}px;
                      height: ${this.height}px;
                      overflow: hidden;
                      border-radius: 150px;
                      box-shadow: 0 8px 24px rgba(6, 12, 24, 0.35), 0 -18px 25px inset rgba(255, 255, 255, 0.1);
                      cursor: grab;
                      backdrop-filter: url(#${this.id}_filter) blur(0.25px) contrast(1.2) brightness(1.05) saturate(1.1);
                      z-index: 9999;
                      pointer-events: auto;
                    `;

                    this.svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
                    this.svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
                    this.svg.setAttribute('width', '0');
                    this.svg.setAttribute('height', '0');
                    this.svg.style.cssText = `
                      position: fixed;
                      top: 0;
                      left: 0;
                      pointer-events: none;
                      z-index: 9998;
                    `;

                    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
                    const filter = document.createElementNS('http://www.w3.org/2000/svg', 'filter');
                    filter.setAttribute('id', `${this.id}_filter`);
                    filter.setAttribute('filterUnits', 'userSpaceOnUse');
                    filter.setAttribute('color-interpolation-filters', 'sRGB');
                    filter.setAttribute('x', '0');
                    filter.setAttribute('y', '0');
                    filter.setAttribute('width', this.width.toString());
                    filter.setAttribute('height', this.height.toString());

                    this.feImage = document.createElementNS('http://www.w3.org/2000/svg', 'feImage');
                    this.feImage.setAttribute('id', `${this.id}_map`);
                    this.feImage.setAttribute('width', this.width.toString());
                    this.feImage.setAttribute('height', this.height.toString());

                    this.feDisplacementMap = document.createElementNS('http://www.w3.org/2000/svg', 'feDisplacementMap');
                    this.feDisplacementMap.setAttribute('in', 'SourceGraphic');
                    this.feDisplacementMap.setAttribute('in2', `${this.id}_map`);
                    this.feDisplacementMap.setAttribute('xChannelSelector', 'R');
                    this.feDisplacementMap.setAttribute('yChannelSelector', 'G');

                    filter.appendChild(this.feImage);
                    filter.appendChild(this.feDisplacementMap);
                    defs.appendChild(filter);
                    this.svg.appendChild(defs);

                    this.canvas = document.createElement('canvas');
                    this.canvas.width = this.width * this.canvasDPI;
                    this.canvas.height = this.height * this.canvasDPI;
                    this.canvas.style.display = 'none';

                    this.context = this.canvas.getContext('2d');
                  }

                  constrainPosition(x, y) {
                    const viewportWidth = window.innerWidth;
                    const viewportHeight = window.innerHeight;
                    
                    const minX = this.offset;
                    const maxX = viewportWidth - this.width - this.offset;
                    const minY = this.offset;
                    const maxY = viewportHeight - this.height - this.offset;
                    
                    const constrainedX = Math.max(minX, Math.min(maxX, x));
                    const constrainedY = Math.max(minY, Math.min(maxY, y));
                    
                    return { x: constrainedX, y: constrainedY };
                  }

                  setupEventListeners() {
                    let isDragging = false;
                    let startX, startY, initialX, initialY;

                    this.container.addEventListener('mousedown', (e) => {
                      isDragging = true;
                      this.container.style.cursor = 'grabbing';
                      startX = e.clientX;
                      startY = e.clientY;
                      const rect = this.container.getBoundingClientRect();
                      initialX = rect.left;
                      initialY = rect.top;
                      e.preventDefault();
                    });

                    document.addEventListener('mousemove', (e) => {
                      if (isDragging) {
                        const deltaX = e.clientX - startX;
                        const deltaY = e.clientY - startY;
                        const newX = initialX + deltaX;
                        const newY = initialY + deltaY;
                        const constrained = this.constrainPosition(newX, newY);
                        this.container.style.left = constrained.x + 'px';
                        this.container.style.top = constrained.y + 'px';
                        this.container.style.transform = 'none';
                      }

                      const rect = this.container.getBoundingClientRect();
                      this.mouse.x = (e.clientX - rect.left) / rect.width;
                      this.mouse.y = (e.clientY - rect.top) / rect.height;
                      
                      if (this.mouseUsed) {
                        this.updateShader();
                      }
                    });

                    document.addEventListener('mouseup', () => {
                      isDragging = false;
                      this.container.style.cursor = 'grab';
                    });

                    window.addEventListener('resize', () => {
                      const rect = this.container.getBoundingClientRect();
                      const constrained = this.constrainPosition(rect.left, rect.top);
                      
                      if (rect.left !== constrained.x || rect.top !== constrained.y) {
                        this.container.style.left = constrained.x + 'px';
                        this.container.style.top = constrained.y + 'px';
                        this.container.style.transform = 'none';
                      }
                    });
                  }

                  updateShader() {
                    const mouseProxy = new Proxy(this.mouse, {
                      get: (target, prop) => {
                        this.mouseUsed = true;
                        return target[prop];
                      }
                    });

                    this.mouseUsed = false;

                    const w = this.width * this.canvasDPI;
                    const h = this.height * this.canvasDPI;
                    const data = new Uint8ClampedArray(w * h * 4);

                    let maxScale = 0;
                    const rawValues = [];

                    for (let i = 0; i < data.length; i += 4) {
                      const x = (i / 4) % w;
                      const y = Math.floor(i / 4 / w);
                      const pos = this.fragment(
                        { x: x / w, y: y / h },
                        mouseProxy
                      );
                      const dx = pos.x * w - x;
                      const dy = pos.y * h - y;
                      maxScale = Math.max(maxScale, Math.abs(dx), Math.abs(dy));
                      rawValues.push(dx, dy);
                    }

                    maxScale *= 0.5;

                    let index = 0;
                    for (let i = 0; i < data.length; i += 4) {
                      const r = rawValues[index++] / maxScale + 0.5;
                      const g = rawValues[index++] / maxScale + 0.5;
                      data[i] = r * 255;
                      data[i + 1] = g * 255;
                      data[i + 2] = 0;
                      data[i + 3] = 255;
                    }

                    this.context.putImageData(new ImageData(data, w, h), 0, 0);
                    this.feImage.setAttributeNS('http://www.w3.org/1999/xlink', 'href', this.canvas.toDataURL());
                    this.feDisplacementMap.setAttribute('scale', (maxScale / this.canvasDPI).toString());
                  }

                  appendTo(parent) {
                    parent.appendChild(this.svg);
                    parent.appendChild(this.container);
                  }

                  destroy() {
                    this.svg.remove();
                    this.container.remove();
                    this.canvas.remove();
                  }
                }

                function createLiquidGlass() {
                  const shader = new Shader({
                    width: 320,
                    height: 220,
                    fragment: (uv) => {
                      const ix = uv.x - 0.5;
                      const iy = uv.y - 0.5;
                      const distanceToEdge = roundedRectSDF(ix, iy, 0.3, 0.22, 0.55);
                      const displacement = smoothStep(0.8, 0, distanceToEdge - 0.12);
                      const scaled = smoothStep(0, 1, displacement);
                      return texture(ix * scaled + 0.5, iy * scaled + 0.5);
                    }
                  });

                  shader.appendTo(document.body);

                  console.log('Liquid Glass effect created! Drag the glass around the page.');
                  window.liquidGlass = shader;
                }

                createLiquidGlass();
              })();
              </script>
            </body>
            </html>
            """;
    private static final String DATA_URL = "data:text/html;base64," +
            Base64.getEncoder().encodeToString(HELLO_WORLD_HTML.getBytes(StandardCharsets.UTF_8));

    private final WebRendererManager manager = WebRendererManager.INSTANCE;
    private WebRendererInstance instance;
    private int areaX;
    private int areaY;
    private int areaWidth;
    private int areaHeight;

    public HelloWorldWebRendererScreen() {
        super(Text.translatable("xwebrenderer.screen.hello_world.title"));
    }

    @Override
    protected void init() {
        manager.initialize();
        areaX = PADDING;
        areaY = PADDING;
        areaWidth = width - PADDING * 2;
        areaHeight = height - PADDING * 2;
        instance = manager.create(WebRendererConfig.builder()
                .url(DATA_URL)
                .transparent(false)
                .debugGui(false)
                .build());
        if (instance != null) {
            instance.resize(areaWidth, areaHeight);
            instance.setFocused(true);
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, width, height, 0xCC101018);
        drawContext.drawBorder(areaX - 4, areaY - 4, areaWidth + 8, areaHeight + 8, 0xFF3C415A);
        if (instance != null) {
            instance.render(drawContext, areaX, areaY, areaWidth, areaHeight, delta);
        }
        if (!manager.hasRealEngine()) {
            drawContext.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("xwebrenderer.screen.demo.no_engine"),
                    width / 2,
                    height / 2 - 8,
                    0xFFFF5555);
            drawContext.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Install MCEF and reopen the demo."),
                    width / 2,
                    height / 2 + 8,
                    0xFFB0B0B0);
        }
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY) && instance != null) {
            instance.setFocused(true);
            instance.mouseButton(mouseX - areaX, mouseY - areaY, button, true, currentModifiers());
            return true;
        }
        if (instance != null) {
            instance.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (instance != null && isInside(mouseX, mouseY)) {
            instance.mouseButton(mouseX - areaX, mouseY - areaY, button, false, currentModifiers());
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (instance != null) {
            instance.mouseMove(mouseX - areaX, mouseY - areaY);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (instance != null && isInside(mouseX, mouseY)) {
            instance.mouseMove(mouseX - areaX, mouseY - areaY);
            instance.mouseButton(mouseX - areaX, mouseY - areaY, button, true, currentModifiers());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (instance != null && isInside(mouseX, mouseY)) {
            instance.scroll(horizontal, vertical);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (instance != null) {
            instance.keyEvent(keyCode, scanCode, modifiers, true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (instance != null) {
            instance.keyEvent(keyCode, scanCode, modifiers, false);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (instance != null) {
            instance.charTyped(chr);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        areaX = PADDING;
        areaY = PADDING;
        areaWidth = width - PADDING * 2;
        areaHeight = height - PADDING * 2;
        if (instance != null) {
            instance.resize(areaWidth, areaHeight);
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (instance != null) {
            instance.close();
            instance = null;
        }
        manager.shutdown();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean isInside(double mouseX, double mouseY) {
        return mouseX >= areaX && mouseX < areaX + areaWidth && mouseY >= areaY && mouseY < areaY + areaHeight;
    }

    private int currentModifiers() {
        int modifiers = 0;
        if (hasShiftDown()) {
            modifiers |= 0x1;
        }
        if (hasControlDown()) {
            modifiers |= 0x2;
        }
        if (hasAltDown()) {
            modifiers |= 0x4;
        }
        return modifiers;
    }
}
