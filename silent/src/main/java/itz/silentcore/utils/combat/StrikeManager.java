package itz.silentcore.utils.combat;

import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.math.Calculate;
import itz.silentcore.utils.math.StopWatch;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.Turns;
import itz.silentcore.utils.rotation.TurnsConnection;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Setter
@Getter
public class StrikeManager implements IMinecraft {
    private final StopWatch attackTimer = new StopWatch();
    private final StopWatch shieldWatch = new StopWatch();
    private final StopWatch sprintCooldown = new StopWatch();
    private int count = 0;
    private boolean prevSprinting;

    public void tick() {
        // Обновление таймеров
    }

    public void handleAttack(AttackConfig config) {
        if (!canAttack(config, 0)) return;
        
        preAttackEntity(config);
        
        // Проверка raycast к цели
        if (!rayTraceTarget(config)) return;
        if (!canAttack(config, 0)) return;
        
        String sprintMode = config.sprintMode;
        if (sprintMode.equals("Legit") && !isSprinting()) {
            attackEntity(config);
        }
        
        if (sprintMode.equals("Packet")) {
            mc.player.setSprinting(false);
            // Отправка пакета спринта происходит автоматически
            attackEntity(config);
        }
    }

    private void preAttackEntity(AttackConfig config) {
        // Сброс щита если нужно
        if (config.shouldUnPressShield && mc.player.isUsingItem() 
                && mc.player.getActiveItem().getItem().equals(Items.SHIELD)) {
            mc.interactionManager.stopUsingItem(mc.player);
            shieldWatch.reset();
        }
        
        // Сброс спринта для критов (Legit режим)
        String sprintMode = config.sprintMode;
        if (sprintMode.equals("Legit")) {
            if (mc.player.isSprinting() && mc.player.distanceTo(config.target) <= config.maximumRange) {
                itz.silentcore.feature.module.impl.movement.AutoSprint.tickStop = 2;
                mc.options.sprintKey.setPressed(false);
                mc.player.setSprinting(false);
                return;
            }
        }
    }

    private void attackEntity(AttackConfig config) {
        attack(config);
        breakShield(config);
        attackTimer.reset();
        count++;
    }

    private void postAttackEntity(AttackConfig config) {
        // Пост-обработка после атаки
    }

    private void breakShield(AttackConfig config) {
        LivingEntity target = config.target;
        Turns angleToPlayer = MathAngle.fromVec3d(
            mc.player.getBoundingBox().getCenter().subtract(target.getEyePos())
        );
        
        boolean targetOnShield = target.isUsingItem() && target.getActiveItem().getItem().equals(Items.SHIELD);
        boolean angle = Math.abs(computeAngleDifference(target.getYaw(), angleToPlayer.getYaw())) < 90;
        
        // Проверка наличия топора в инвентаре
        boolean hasAxe = false;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                hasAxe = true;
                break;
            }
        }
        
        if (config.shouldBreakShield && targetOnShield && hasAxe && angle) {
            // Логика смены на топор и атаки
            // Упрощенная версия без InventoryTask
        }
    }

    private void attack(AttackConfig config) {
        float chance = Calculate.getRandom(0, 100);
        
        if (config.useHitChance && chance < config.hitChance) {
            mc.interactionManager.attackEntity(mc.player, config.target);
        } else if (!config.useHitChance) {
            mc.interactionManager.attackEntity(mc.player, config.target);
        }
        
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isSprinting() {
        return mc.player.isSprinting() && !mc.player.isInLava() && !mc.player.isTouchingWater();
    }

    /**
     * Проверка возможности атаки с симуляцией на N тиков вперед
     */
    public boolean canAttack(AttackConfig config, int ticks) {
        for (int i = 0; i <= ticks; i++) {
            if (canCrit(config, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверка возможности крита с симуляцией
     */
    public boolean canCrit(AttackConfig config, int ticks) {
        if (mc.player == null) return false;
        
        // Проверка использования предмета
        if (mc.player.isUsingItem() && !mc.player.getActiveItem().getItem().equals(Items.SHIELD) 
                && config.noAttackWhenEat) {
            return false;
        }

        // Проверка cooldown атаки
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f) {
            return false;
        }

        // Если не требуются криты - можно атаковать
        if (!config.onlyCritical) {
            return true;
        }

        // Проверка ограничений движения
        boolean hasRestrictions = mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isClimbing()
                || mc.player.getAbilities().flying
                || mc.player.hasVehicle();
        
        // Если есть ограничения - разрешаем атаку (не можем сделать крит)
        if (hasRestrictions) {
            return true;
        }
        
        // Умные криты - атакуем в оптимальный момент
        if (config.smartCrits) {
            boolean onGround = mc.player.isOnGround();
            boolean falling = mc.player.getVelocity().y < 0;
            double velocityY = mc.player.getVelocity().y;
            
            // Разрешаем атаку если:
            // 1. В воздухе и падаем (независимо от fallDistance)
            // 2. На земле - разрешаем атаку, игрок сам прыгнет
            if (!onGround && falling) {
                return true;
            }
            
            // На земле - разрешаем атаку
            if (onGround) {
                return true;
            }
            
            return false;
        }
        
        // Обычные криты - только в воздухе с падением
        boolean onGround = mc.player.isOnGround();
        boolean falling = mc.player.getVelocity().y < 0;
        boolean hasFallDistance = mc.player.fallDistance > 0.0f;
        
        return !onGround && falling && hasFallDistance;
    }

    /**
     * Симуляция состояния игрока через N тиков
     */
    private PlayerSimulation simulatePlayer(int ticks) {
        PlayerSimulation sim = new PlayerSimulation();
        sim.onGround = mc.player.isOnGround();
        sim.fallDistance = mc.player.fallDistance;
        sim.velocityY = mc.player.getVelocity().y;
        sim.pos = mc.player.getPos();
        sim.boundingBox = mc.player.getBoundingBox();
        
        // Симулируем движение на N тиков вперед
        for (int i = 0; i < ticks; i++) {
            if (!sim.onGround) {
                // Применяем гравитацию
                sim.velocityY -= 0.08;
                sim.velocityY *= 0.98; // Сопротивление воздуха
                
                // Обновляем fallDistance
                if (sim.velocityY < 0) {
                    sim.fallDistance += Math.abs(sim.velocityY);
                }
            } else {
                // Если на земле, проверяем прыжок
                if (mc.options.jumpKey.isPressed()) {
                    sim.velocityY = 0.42; // Начальная скорость прыжка
                    sim.onGround = false;
                    sim.fallDistance = 0;
                }
            }
            
            // Обновляем позицию
            sim.pos = sim.pos.add(0, sim.velocityY, 0);
            
            // Проверяем приземление (упрощенно)
            if (sim.velocityY < 0 && sim.pos.y <= mc.player.getY()) {
                sim.onGround = true;
                sim.fallDistance = 0;
                sim.velocityY = 0;
            }
        }
        
        return sim;
    }

    /**
     * Проверка ограничений движения
     */
    private boolean hasMovementRestrictions(PlayerSimulation simulated) {
        return mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || simulated.isSubmergedInWater()
                || simulated.isInLava()
                || simulated.isClimbing()
                || mc.player.getAbilities().flying;
    }

    /**
     * Проверка критического состояния
     */
    private boolean isPlayerInCriticalState(PlayerSimulation simulated, int ticks) {
        boolean fall = simulated.fallDistance > 0;
        boolean falling = simulated.velocityY < 0;
        return !simulated.onGround && fall && falling;
    }

    /**
     * Raycast к цели
     */
    private boolean rayTraceTarget(AttackConfig config) {
        if (mc.player == null || config.target == null) return false;
        
        Vec3d eyePos = mc.player.getEyePos();
        Turns rotation = TurnsConnection.INSTANCE.getRotation();
        Vec3d lookVec = rotation.toVector();
        
        Box targetBox = config.target.getBoundingBox();
        Vec3d endPos = eyePos.add(lookVec.multiply(config.maximumRange));
        
        return targetBox.raycast(eyePos, endPos).isPresent();
    }

    /**
     * Вычисление разницы углов
     */
    private static float computeAngleDifference(float angle1, float angle2) {
        float diff = angle1 - angle2;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }

    /**
     * Класс для симуляции состояния игрока
     */
    public static class PlayerSimulation {
        public boolean onGround;
        public float fallDistance;
        public double velocityY;
        public Vec3d pos;
        public Box boundingBox;
        
        public boolean isSubmergedInWater() {
            return false; // Упрощенная версия
        }
        
        public boolean isInLava() {
            return false; // Упрощенная версия
        }
        
        public boolean isClimbing() {
            return false; // Упрощенная версия
        }
    }

    @Getter
    public static class AttackConfig {
        private final LivingEntity target;
        private final float maximumRange;
        private final boolean onlyCritical;
        private final boolean shouldBreakShield;
        private final boolean shouldUnPressShield;
        private final boolean noAttackWhenEat;
        private final boolean useHitChance;
        private final float hitChance;
        private final String sprintMode;
        private final boolean smartCrits;

        public AttackConfig(LivingEntity target, float maximumRange, List<String> options, 
                          float hitChance, String sprintMode, boolean smartCrits) {
            this.target = target;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Critical");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.noAttackWhenEat = options.contains("No Attack When Eat");
            this.useHitChance = options.contains("Hit Chance");
            this.hitChance = hitChance;
            this.sprintMode = sprintMode;
            this.smartCrits = smartCrits;
        }
    }
}
