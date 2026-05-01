package itz.silentcore.feature.module.impl.misc;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.InputEvent;
import itz.silentcore.feature.event.impl.KeyEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.interact.PlayerInteractionHelper;
import itz.silentcore.utils.inventory.InventoryResult;
import itz.silentcore.utils.inventory.InventoryTask;
import itz.silentcore.utils.inventory.InventoryToolkit;
import itz.silentcore.utils.script.Script;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@ModuleAnnotation(name = "ElytraHelper", category = Category.MISC, description = "Elytra swap and firework helper")
public class ElytraHelper extends Module {
    private final ModeSetting modeSetting = new ModeSetting("Mode", "Swap mode", "Default", "Legit");
    private final BooleanSetting startSetting = new BooleanSetting("Quick Start", "Auto fly on swap", false);
    private final BooleanSetting recast = new BooleanSetting("Auto Fly", "Auto start flying", false);
    private final BooleanSetting autoFireworkSetting = new BooleanSetting("Auto Firework", "Auto use firework", false);
    private final NumberSetting fireworkDelay = new NumberSetting("Firework Delay", 500f, 10f, 1500f, 10f, "Delay in ms");

    private final Script script = new Script();
    
    private int elytraSwapKey = GLFW.GLFW_KEY_R;
    private int fireworkKey = GLFW.GLFW_KEY_G;

    enum ElytraPhase { READY, SLOWING_DOWN, WAITING_STOP, SWAP, SPEEDING_UP, FINISHED }
    ElytraPhase elytraPhase = ElytraPhase.READY;
    long actionStartTime = 0L;
    Slot targetSlot = null;
    boolean playerFullyStopped = false;
    boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;
    boolean keysOverridden = false;

    enum FireworkPhase { READY, START, SWAP_TO_HOTBAR, WAITING_TO_USE, USE_FIREWORK, SWAP_BACK_TO_INV, FINISH }
    FireworkPhase fireworkPhase = FireworkPhase.READY;
    int fireworkSlot = -1;
    int savedHotbarSlot = -1;
    int originalHotbarSlot = -1;
    long useDelayStartTime = 0L;
    long lastAutoFireworkTime = 0L;

    @Subscribe
    public void onInput(InputEvent e) {
        if (mc.player != null && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) && recast.isEnabled()) {
            if (mc.player.isOnGround()) {
                // Set jump through options instead of Input field
                mc.options.jumpKey.setPressed(true);
            } else if (!mc.player.isGliding()) {
                PlayerInteractionHelper.startFallFlying();
            }
        }
    }

    @Subscribe
    public void onKey(KeyEvent e) {
        if (!script.isFinished()) return;

        if (e.isKeyDown(elytraSwapKey)) {
            if (modeSetting.get().equals("Default")) {
                executeDefaultSwap();
            } else if (modeSetting.get().equals("Legit") && elytraPhase == ElytraPhase.READY) {
                startLegitSwap();
            }
        } else if (e.isKeyDown(fireworkKey) && mc.player != null && mc.player.isGliding() && fireworkPhase == FireworkPhase.READY) {
            fireworkPhase = FireworkPhase.START;
        }
    }

    @Subscribe
    public void onTick(TickEvent e) {
        script.update();

        if (elytraPhase != ElytraPhase.READY) {
            processLegitSwap();
        }

        if (fireworkPhase != FireworkPhase.READY) {
            if (modeSetting.get().equals("Default")) {
                processDefaultFireworkUsage();
            } else {
                processLegitFireworkUsage();
            }
        }

        if (autoFireworkSetting.isEnabled() && mc.player != null && mc.player.isGliding()) {
            long now = System.currentTimeMillis();
            if (now - lastAutoFireworkTime >= (long) fireworkDelay.getCurrent()) {
                if (fireworkPhase == FireworkPhase.READY && elytraPhase == ElytraPhase.READY) {
                    if (modeSetting.get().equals("Default")) {
                        InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.FIREWORK_ROCKET);
                        if (hotbar.found()) {
                            InventoryTask.swapAndUse(Items.FIREWORK_ROCKET);
                            lastAutoFireworkTime = now;
                        } else {
                            InventoryResult inv = InventoryToolkit.findItemInInventory(Items.FIREWORK_ROCKET);
                            if (inv.found()) {
                                int fireworkInvSlot = inv.slot();
                                int currentHotbarSlot = mc.player.getInventory().selectedSlot;
                                InventoryToolkit.clickSlot(fireworkInvSlot, currentHotbarSlot, SlotActionType.SWAP);
                                PlayerInteractionHelper.interactItem(Hand.MAIN_HAND);
                                InventoryToolkit.clickSlot(fireworkInvSlot, currentHotbarSlot, SlotActionType.SWAP);
                                lastAutoFireworkTime = now;
                            }
                        }
                    } else {
                        fireworkPhase = FireworkPhase.START;
                        lastAutoFireworkTime = now;
                    }
                }
            }
        }
    }

    private void processDefaultFireworkUsage() {
        if (mc.player == null) {
            resetFireworkState();
            return;
        }

        InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.FIREWORK_ROCKET);
        if (hotbar.found()) {
            InventoryTask.swapAndUse(Items.FIREWORK_ROCKET);
        } else {
            InventoryResult inv = InventoryToolkit.findItemInInventory(Items.FIREWORK_ROCKET);
            if (inv.found()) {
                int fireworkInvSlot = inv.slot();
                int currentHotbarSlot = mc.player.getInventory().selectedSlot;
                InventoryToolkit.clickSlot(fireworkInvSlot, currentHotbarSlot, SlotActionType.SWAP);
                PlayerInteractionHelper.interactItem(Hand.MAIN_HAND);
                InventoryToolkit.clickSlot(fireworkInvSlot, currentHotbarSlot, SlotActionType.SWAP);
            }
        }
        resetFireworkState();
    }

    private void processLegitFireworkUsage() {
        if (mc.player == null || mc.currentScreen != null) {
            resetFireworkState();
            return;
        }

        switch (fireworkPhase) {
            case START -> {
                originalHotbarSlot = mc.player.getInventory().selectedSlot;
                InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.FIREWORK_ROCKET);
                if (hotbar.found()) {
                    savedHotbarSlot = hotbar.slot();
                    InventoryToolkit.switchTo(savedHotbarSlot);
                    useDelayStartTime = System.currentTimeMillis();
                    fireworkPhase = FireworkPhase.WAITING_TO_USE;
                } else {
                    InventoryResult inv = InventoryToolkit.findItemInInventory(Items.FIREWORK_ROCKET);
                    if (inv.found()) {
                        fireworkSlot = inv.slot();
                        savedHotbarSlot = originalHotbarSlot;
                        fireworkPhase = FireworkPhase.SWAP_TO_HOTBAR;
                    } else {
                        resetFireworkState();
                    }
                }
            }
            case SWAP_TO_HOTBAR -> {
                InventoryToolkit.clickSlot(fireworkSlot, savedHotbarSlot, SlotActionType.SWAP);
                useDelayStartTime = System.currentTimeMillis();
                fireworkPhase = FireworkPhase.WAITING_TO_USE;
            }
            case WAITING_TO_USE -> {
                if (System.currentTimeMillis() - useDelayStartTime > 20) {
                    fireworkPhase = FireworkPhase.USE_FIREWORK;
                }
            }
            case USE_FIREWORK -> {
                if (mc.player.getMainHandStack().getItem() == Items.FIREWORK_ROCKET) {
                    PlayerInteractionHelper.interactItem(Hand.MAIN_HAND);
                }
                fireworkPhase = (fireworkSlot != -1) ? FireworkPhase.SWAP_BACK_TO_INV : FireworkPhase.FINISH;
            }
            case SWAP_BACK_TO_INV -> {
                InventoryToolkit.clickSlot(fireworkSlot, savedHotbarSlot, SlotActionType.SWAP);
                fireworkPhase = FireworkPhase.FINISH;
            }
            case FINISH -> resetFireworkState();
        }
    }

    private void resetFireworkState() {
        if (originalHotbarSlot != -1 && mc.player != null) {
            InventoryToolkit.switchTo(originalHotbarSlot);
        }
        fireworkPhase = FireworkPhase.READY;
        fireworkSlot = -1;
        savedHotbarSlot = -1;
        originalHotbarSlot = -1;
        useDelayStartTime = 0L;
    }

    private void executeDefaultSwap() {
        Slot slot = chestPlate();

        if (slot != null) {
            Slot fireWork = InventoryTask.getSlot(Items.FIREWORK_ROCKET);
            boolean elytra = slot.getStack().getItem().equals(Items.ELYTRA);
            InventoryTask.moveItem(slot, 6, false, true);

            if (startSetting.isEnabled() && fireWork != null && elytra) {
                script.cleanup().addTickStep(4, () -> {
                    if (mc.player.isOnGround()) mc.player.jump();
                }).addTickStep(3, () -> {
                    PlayerInteractionHelper.startFallFlying();
                    InventoryTask.swapAndUse(Items.FIREWORK_ROCKET);
                });
            }
        }
    }

    private void startLegitSwap() {
        targetSlot = chestPlate();
        if (targetSlot == null) return;

        wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        elytraPhase = ElytraPhase.SLOWING_DOWN;
        actionStartTime = System.currentTimeMillis();
        playerFullyStopped = false;
        keysOverridden = false;
    }

    private void processLegitSwap() {
        if (mc.player == null || mc.currentScreen != null) {
            resetLegitState();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (elytraPhase) {
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
                    elytraPhase = ElytraPhase.WAITING_STOP;
                }
            }
            case WAITING_STOP -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                double velocityX = Math.abs(mc.player.getVelocity().x);
                double velocityZ = Math.abs(mc.player.getVelocity().z);
                if (velocityX < 0.001 && velocityZ < 0.001 || elapsed > 1) {
                    playerFullyStopped = true;
                    elytraPhase = ElytraPhase.SWAP;
                }
            }
            case SWAP -> {
                if (playerFullyStopped) {
                    if (targetSlot != null) {
                        boolean elytra = targetSlot.getStack().getItem().equals(Items.ELYTRA);
                        InventoryTask.moveItem(targetSlot, 6, false, false);

                        if (startSetting.isEnabled() && elytra) {
                            Slot fireWork = InventoryTask.getSlot(Items.FIREWORK_ROCKET);
                            if (fireWork != null) {
                                script.cleanup().addTickStep(2, () -> {
                                    if (mc.player.isOnGround()) mc.player.jump();
                                }).addTickStep(1, () -> {
                                    PlayerInteractionHelper.startFallFlying();
                                    InventoryTask.swapAndUse(Items.FIREWORK_ROCKET);
                                });
                            }
                        }
                    }
                    elytraPhase = ElytraPhase.SPEEDING_UP;
                    actionStartTime = System.currentTimeMillis();

                    if (keysOverridden) {
                        restoreKeyStates();
                    }
                }
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 1.0f);
                if (mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, 0.4f);
                    if (speedupProgress > 0.4f && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }
                if (speedupElapsed > 1) {
                    elytraPhase = ElytraPhase.FINISHED;
                }
            }
            case FINISHED -> resetLegitState();
        }
    }

    private void restoreKeyStates() {
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

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void resetLegitState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        elytraPhase = ElytraPhase.READY;
        targetSlot = null;
        playerFullyStopped = false;
    }

    private Slot chestPlate() {
        if (mc.player == null) return null;
        
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return InventoryTask.getSlot(List.of(
                    Items.NETHERITE_CHESTPLATE,
                    Items.DIAMOND_CHESTPLATE,
                    Items.IRON_CHESTPLATE,
                    Items.GOLDEN_CHESTPLATE,
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.LEATHER_CHESTPLATE
            ));
        } else {
            return InventoryTask.getSlot(Items.ELYTRA);
        }
    }
}
