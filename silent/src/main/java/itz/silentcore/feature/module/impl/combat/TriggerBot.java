package itz.silentcore.feature.module.impl.combat;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@ModuleAnnotation(name = "TriggerBot", category = Category.COMBAT, description = "Автоматически атакует при наведении")
public class TriggerBot extends Module {

    private final NumberSetting attackRange = new NumberSetting("Дистанция атаки", 3.0f, 1.0f, 6.0f, 0.1f);
    private final NumberSetting attackSpeed = new NumberSetting("Скорость атаки", 10.0f, 1.0f, 20.0f, 1.0f);
    
    private final MultiBooleanSetting targets = new MultiBooleanSetting("Цели",
            MultiBooleanSetting.Value.of("Игроки"),
            MultiBooleanSetting.Value.of("Мобы"),
            MultiBooleanSetting.Value.of("Животные"));
    
    private final BooleanSetting onlyCritical = new BooleanSetting("Только криты", false);
    
    private long lastAttackTime = 0;

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Получаем сущность на которую смотрит игрок
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) return;
        
        EntityHitResult entityHit = (EntityHitResult) hitResult;
        Entity entity = entityHit.getEntity();
        
        if (!(entity instanceof LivingEntity target)) return;
        if (!isValidTarget(target)) return;
        
        // Проверка дистанции
        if (mc.player.distanceTo(target) > attackRange.getCurrent()) return;
        
        // Проверка возможности атаки
        if (!canAttack()) return;
        
        // Атака
        attackTarget(target);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == mc.player) return false;
        if (!entity.isAlive()) return false;
        if (entity.getHealth() <= 0) return false;
        
        // Проверка типа цели
        if (entity instanceof PlayerEntity && !targets.isEnable("Игроки")) return false;
        if (entity instanceof Monster && !targets.isEnable("Мобы")) return false;
        if (entity instanceof AnimalEntity && !targets.isEnable("Животные")) return false;
        
        return true;
    }

    private boolean canAttack() {
        if (mc.player == null) return false;
        
        // Проверка cooldown атаки
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return false;
        
        long currentTime = System.currentTimeMillis();
        long attackDelay = (long) (1000.0f / attackSpeed.getCurrent());
        
        if (currentTime - lastAttackTime < attackDelay) return false;
        
        // Проверка критов - игрок должен падать
        if (onlyCritical.isEnabled()) {
            if (mc.player.isOnGround()) return false;
            if (mc.player.isTouchingWater()) return false;
            if (mc.player.hasVehicle()) return false;
            if (mc.player.fallDistance <= 0) return false;
        }
        
        return true;
    }

    private void attackTarget(LivingEntity target) {
        if (mc.interactionManager == null) return;
        
        // Сброс спринта для критов
        if (onlyCritical.isEnabled() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
        
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        
        lastAttackTime = System.currentTimeMillis();
    }
}
