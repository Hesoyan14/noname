package itz.silentcore.feature.ui.hud.drag;

import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.utils.render.ColorRGBA;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class DragComponent {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private float x;
    @Getter
    @Setter
    private float y;
    @Getter
    @Setter
    private float width;
    @Getter
    @Setter
    private float height;
    @Getter
    @Setter
    private boolean draggable = true;
    @Getter
    @Setter
    private boolean allowDragX = true;
    @Getter
    @Setter
    private boolean allowDragY = true;
    @Getter
    private float rotation = 0f;
    private float targetRotation = 0f;
    private float targetX = 0f;
    private float targetY = 0f;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private float prevX = 0f;
    private float prevY = 0f;
    @Getter
    private static DragComponent dragging;
    private static float dragOffsetX;
    private static float dragOffsetY;
    private static boolean wasMouseDown;

    public DragComponent(String id) {
        this.id = id;
    }

    public abstract void render(Render2DEvent event);

    public static boolean isDragging() {
        return dragging != null;
    }

    public static void handleDrag(Render2DEvent event, List<DragComponent> elements) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        boolean chatOpen = mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
        if (!chatOpen) {
            for (DragComponent element : elements) {
                element.rotation = 0f;
                element.targetRotation = 0f;
                element.velocityX = 0f;
                element.velocityY = 0f;
            }
            dragging = null;
            wasMouseDown = false;
            return;
        }

        var ctx = event.getContext();
        Window window = mc.getWindow();

        double rawMouseX = mc.mouse.getX();
        double rawMouseY = mc.mouse.getY();
        float mouseX = (float) (rawMouseX * window.getScaledWidth() / (double) window.getWidth());
        float mouseY = (float) (rawMouseY * window.getScaledHeight() / (double) window.getHeight());

        boolean mouseDown = GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

        if (mouseDown && !wasMouseDown) {
            if (dragging == null) {
                for (int i = elements.size() - 1; i >= 0; i--) {
                    DragComponent element = elements.get(i);
                    if (!element.isDraggable()) continue;
                    if (mouseX >= element.getX() && mouseX <= element.getX() + element.getWidth()
                            && mouseY >= element.getY() && mouseY <= element.getY() + element.getHeight()) {
                        dragging = element;
                        dragOffsetX = mouseX - element.getX();
                        dragOffsetY = mouseY - element.getY();
                        element.prevX = element.x;
                        element.prevY = element.y;
                        element.targetX = element.x;
                        element.targetY = element.y;
                        element.velocityX = 0f;
                        element.velocityY = 0f;
                        break;
                    }
                }
            }
        }

        if (mouseDown && dragging != null) {
            float newX = mouseX - dragOffsetX;
            float newY = mouseY - dragOffsetY;
            float snappedX = newX;
            float snappedY = newY;

            float screenW = ctx.getContext().getScaledWindowWidth();
            float screenH = ctx.getContext().getScaledWindowHeight();

            List<Float> vLines = new ArrayList<>();
            List<Float> hLines = new ArrayList<>();
            vLines.add(screenW / 2f);
            hLines.add(screenH / 2f);
            if (dragging.isAllowDragX()) {
                float bestDist = 9f;
                float bestX = newX;
                float w = dragging.getWidth();
                for (float lineX : vLines) {
                    float candLeft = lineX;
                    float candCenter = lineX - w / 2f;
                    float candRight = lineX - w;

                    float dLeft = Math.abs(newX - candLeft);
                    float dCenter = Math.abs(newX - candCenter);
                    float dRight = Math.abs(newX - candRight);

                    if (dLeft < bestDist) { bestDist = dLeft; bestX = candLeft; }
                    if (dCenter < bestDist) { bestDist = dCenter; bestX = candCenter; }
                    if (dRight < bestDist) { bestDist = dRight; bestX = candRight; }
                }
                if (bestDist <= 8f) snappedX = bestX;
            }
            if (dragging.isAllowDragY()) {
                float bestDistY = 9f;
                float bestY = newY;
                float h = dragging.getHeight();
                for (float lineY : hLines) {
                    float candTop = lineY;
                    float candCenter = lineY - h / 2f;
                    float candBottom = lineY - h;

                    float dTop = Math.abs(newY - candTop);
                    float dCenter = Math.abs(newY - candCenter);
                    float dBottom = Math.abs(newY - candBottom);

                    if (dTop < bestDistY) { bestDistY = dTop; bestY = candTop; }
                    if (dCenter < bestDistY) { bestDistY = dCenter; bestY = candCenter; }
                    if (dBottom < bestDistY) { bestDistY = dBottom; bestY = candBottom; }
                }
                if (bestDistY <= 8f) snappedY = bestY;
            }
            float maxX = Math.max(0f, screenW - dragging.getWidth());
            float maxY = Math.max(0f, screenH - dragging.getHeight());
            snappedX = Math.max(0f, Math.min(snappedX, maxX));
            snappedY = Math.max(0f, Math.min(snappedY, maxY));

            if (dragging.isAllowDragX()) {
                dragging.targetX = snappedX;
            }
            if (dragging.isAllowDragY()) {
                dragging.targetY = snappedY;
            }

            float oldX = dragging.x;
            float oldY = dragging.y;

            if (dragging.isAllowDragX()) {
                dragging.x = dragging.x + (dragging.targetX - dragging.x) * 0.35f;
                dragging.velocityX = dragging.x - dragging.prevX;
                dragging.prevX = dragging.x;
            }
            if (dragging.isAllowDragY()) {
                dragging.y = dragging.y + (dragging.targetY - dragging.y) * 0.35f;
                dragging.velocityY = dragging.y - dragging.prevY;
                dragging.prevY = dragging.y;
            }

            dragging.updateRotation();
        }

        if (!mouseDown && wasMouseDown) {
            if (dragging != null) {
                DragPositionManager.save(elements);
            }
            dragging = null;
        }

        for (DragComponent element : elements) {
            if (element == dragging) continue;

            float screenW = ctx.getContext().getScaledWindowWidth();
            float screenH = ctx.getContext().getScaledWindowHeight();
            float maxX = Math.max(0f, screenW - element.getWidth());
            float maxY = Math.max(0f, screenH - element.getHeight());

            if (element.isAllowDragX() && Math.abs(element.velocityX) > 0.01f) {
                float newX = element.x + element.velocityX;
                newX = Math.max(0f, Math.min(newX, maxX));
                element.setX(newX);
                element.velocityX *= 0.94f;
            } else {
                element.velocityX = 0f;
            }

            if (element.isAllowDragY() && Math.abs(element.velocityY) > 0.01f) {
                float newY = element.y + element.velocityY;
                newY = Math.max(0f, Math.min(newY, maxY));
                element.setY(newY);
                element.velocityY *= 0.94f;
            } else {
                element.velocityY = 0f;
            }

            element.updateRotation();
        }

        wasMouseDown = mouseDown;
        if (dragging != null) {
            float screenW = ctx.getContext().getScaledWindowWidth();
            float screenH = ctx.getContext().getScaledWindowHeight();
            ColorRGBA centerColor = ColorRGBA.of(255, 255, 255, 255);
            ctx.drawRect(screenW / 2f, 0, 1, screenH, 0.8f, centerColor);
            ctx.drawRect(0, screenH / 2f, screenW, 1, 0.8f, centerColor);

            var matrices = ctx.getContext().getMatrices();
            float pad = 3.0f;
            float x = dragging.getX() - pad;
            float y = dragging.getY() - pad;
            float w = dragging.getWidth() + pad * 2;
            float h = dragging.getHeight() + pad * 2;

            matrices.push();
            matrices.translate((x + w / 2f), (y + h / 2f), 0);
            matrices.multiply(new org.joml.Quaternionf().rotateZ((float) Math.toRadians(dragging.getRotation())));
            matrices.translate(-(x + w / 2f), -(y + h / 2f), 0);

            // Removed white outline while dragging

            matrices.pop();
        }
    }

    private void updateRotation() {
        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);

        if (speed > 0.1f) {
            double angleRad = Math.atan2(velocityY, velocityX);
            targetRotation = (float) Math.toDegrees(angleRad);
            float tiltAmount = Math.min(speed * 1.2f, 8.0f);

            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                targetRotation = velocityX > 0 ? tiltAmount : -tiltAmount;
            } else {
                targetRotation = velocityY > 0 ? tiltAmount : -tiltAmount;
            }
        } else {
            targetRotation = 0f;
        }

        rotation = rotation + (targetRotation - rotation) * 0.08f;

        if (Math.abs(rotation) < 0.1f) {
            rotation = 0f;
        }
    }
}
