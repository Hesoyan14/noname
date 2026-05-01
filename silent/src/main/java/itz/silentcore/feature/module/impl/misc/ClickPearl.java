package itz.silentcore.feature.module.impl.misc;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.utils.inventory.InventoryResult;
import itz.silentcore.utils.inventory.InventoryToolkit;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

@ModuleAnnotation(name = "ClickPearl", category = Category.MISC, description = "Throw ender pearl with one click")
public class ClickPearl extends Module {
    private final ModeSetting modeSetting = new ModeSetting("Mode", "Throw mode", "Default", "Legit");

    private boolean prevKeyPressed = false;
    private long lastThrowTime = 0L;
    private int packetSequence = 0;

    private int savedSlot = -1;
    private int pearlSlot = -1;
    private long actionStartTime = 0L;
    private boolean keysOverridden = false;
    private boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;

    private enum Phase { READY, SLOWING_DOWN, PREPARE, AWAIT_SWITCH, THROW, SPEEDING_UP, FINISH }
    private Phase phase = Phase.READY;

    @Subscribe
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) {
            resetState();
            return;
        }

        boolean keyDown = isBindActive();
        if (!prevKeyPressed && keyDown && System.currentTimeMillis() - lastThrowTime > 100 && phase == Phase.READY) {
            lastThrowTime = System.currentTimeMillis();
            startPearlProcess();
        }
        prevKeyPressed = keyDown;

        if (phase != Phase.READY) {
            execute();
        }
    }

    private void startPearlProcess() {
        if (mc.currentScreen != null) return;

        savedSlot = mc.player.getInventory().selectedSlot;
        InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.ENDER_PEARL);
        if (hotbar.found()) {
            pearlSlot = hotbar.slot();
            InventoryToolkit.switchTo(pearlSlot);
            phase = Phase.AWAIT_SWITCH;
            return;
        }

        InventoryResult inv = InventoryToolkit.findItemInInventory(Items.ENDER_PEARL);
        if (inv.found()) {
            pearlSlot = inv.slot();
            if (modeSetting.get().equals("Legit")) {
                wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
                wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
                wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
                wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

                phase = Phase.SLOWING_DOWN;
                actionStartTime = System.currentTimeMillis();
            } else {
                phase = Phase.PREPARE;
            }
        } else {
            resetState();
        }
    }

    private void execute() {
        if (mc.player == null || mc.currentScreen != null) {
            resetState();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (phase) {
            case SLOWING_DOWN -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                }
                if (!keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }
                if (elapsed > 1) {
                    phase = Phase.PREPARE;
                }
            }
            case PREPARE -> {
                int quickSwapSlot = mc.player.getInventory().selectedSlot;
                InventoryToolkit.clickSlot(pearlSlot, quickSwapSlot, SlotActionType.SWAP);
                InventoryToolkit.switchTo(quickSwapSlot);
                phase = Phase.AWAIT_SWITCH;
            }
            case AWAIT_SWITCH -> {
                if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
                    phase = Phase.THROW;
                }
            }
            case THROW -> {
                InventoryToolkit.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, packetSequence++, mc.player.getYaw(), mc.player.getPitch()));
                mc.player.swingHand(Hand.MAIN_HAND);

                boolean fromInventory = pearlSlot >= 9 && pearlSlot <= 35;
                if (fromInventory) {
                    int quickSwapSlot = mc.player.getInventory().selectedSlot;
                    InventoryToolkit.clickSlot(pearlSlot, quickSwapSlot, SlotActionType.SWAP);
                }
                InventoryToolkit.switchTo(savedSlot);

                if (modeSetting.get().equals("Legit") && fromInventory) {
                    restoreKeyStates();
                    actionStartTime = System.currentTimeMillis();
                    phase = Phase.SPEEDING_UP;
                } else {
                    phase = Phase.FINISH;
                }
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 20.0f);
                if (mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, 0.4f);
                    if (speedupProgress > 0.4f && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }
                if (speedupElapsed > 25) {
                    phase = Phase.FINISH;
                }
            }
            case FINISH -> resetState();
        }
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void restoreKeyStates() {
        if (!keysOverridden) return;
        boolean currentForward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        boolean currentBack = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        boolean currentLeft = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        boolean currentRight = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        boolean currentJump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        mc.options.forwardKey.setPressed(wasForwardPressed && currentForward);
        mc.options.backKey.setPressed(wasBackPressed && currentBack);
        mc.options.leftKey.setPressed(wasLeftPressed && currentLeft);
        mc.options.rightKey.setPressed(wasRightPressed && currentRight);
        mc.options.jumpKey.setPressed(wasJumpPressed && currentJump);
        keysOverridden = false;
    }

    private void resetState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        pearlSlot = -1;
        savedSlot = -1;
        actionStartTime = 0L;
        phase = Phase.READY;
    }

    private boolean isBindActive() {
        long window = mc.getWindow().getHandle();
        int keyCode = this.getKey();

        if (keyCode >= GLFW.GLFW_MOUSE_BUTTON_1 && keyCode <= GLFW.GLFW_MOUSE_BUTTON_8) {
            return GLFW.glfwGetMouseButton(window, keyCode) == GLFW.GLFW_PRESS;
        }
        return InputUtil.isKeyPressed(window, keyCode);
    }
}
