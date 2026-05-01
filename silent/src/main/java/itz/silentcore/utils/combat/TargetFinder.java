package itz.silentcore.utils.combat;

import itz.silentcore.feature.module.impl.combat.AntiBot;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.Turns;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
public class TargetFinder implements IMinecraft {
    private final MultiPoint pointFinder = new MultiPoint();
    private LivingEntity currentTarget;
    private Stream<LivingEntity> potentialTargets;

    public TargetFinder() {
        this.currentTarget = null;
    }

    public void lockTarget(LivingEntity target) {
        if (this.currentTarget == null) {
            this.currentTarget = target;
        }
    }

    public void releaseTarget() {
        this.currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        findFirstMatch(predicate).ifPresent(this::lockTarget);

        if (this.currentTarget != null && !predicate.test(this.currentTarget)) {
            releaseTarget();
        }
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        if (currentTarget != null && (!pointFinder.hasValidPoint(currentTarget, maxDistance, ignoreWalls) 
                || getFov(currentTarget, maxDistance, ignoreWalls) > maxFov)) {
            releaseTarget();
        }

        this.potentialTargets = createStreamFromEntities(entities, maxDistance, maxFov, ignoreWalls);
    }

    private double getFov(LivingEntity entity, float maxDistance, boolean ignoreWalls) {
        if (!pointFinder.hasValidPoint(entity, maxDistance, ignoreWalls)) {
            return 360;
        }
        
        Turns currentAngle = MathAngle.cameraAngle();
        Turns targetAngle = MathAngle.calculateAngle(entity.getEyePos());
        
        return MathAngle.computeRotationDifference(currentAngle, targetAngle);
    }

    private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        return StreamSupport.stream(entities.spliterator(), false)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> pointFinder.hasValidPoint(entity, maxDistance, ignoreWalls) 
                        && getFov(entity, maxDistance, ignoreWalls) < maxFov)
                .sorted(Comparator.comparingDouble(entity -> entity.distanceTo(mc.player)));
    }

    private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> predicate) {
        return this.potentialTargets.filter(predicate).findFirst();
    }

    public static class EntityFilter {
        private final List<String> targetSettings;

        public EntityFilter(List<String> targetSettings) {
            this.targetSettings = targetSettings;
        }

        public boolean isValid(LivingEntity entity) {
            if (isLocalPlayer(entity)) return false;
            if (isInvalidHealth(entity)) return false;
            if (isBotPlayer(entity)) return false;
            return isValidEntityType(entity);
        }

        private boolean isLocalPlayer(LivingEntity entity) {
            return entity == IMinecraft.mc.player;
        }

        private boolean isInvalidHealth(LivingEntity entity) {
            return !entity.isAlive() || entity.getHealth() <= 0;
        }

        private boolean isBotPlayer(LivingEntity entity) {
            return entity instanceof PlayerEntity && AntiBot.isBot((PlayerEntity) entity);
        }

        private boolean isValidEntityType(LivingEntity entity) {
            if (entity instanceof PlayerEntity) {
                return targetSettings.contains("Players");
            } else if (entity instanceof AnimalEntity) {
                return targetSettings.contains("Animals");
            } else if (entity instanceof MobEntity) {
                return targetSettings.contains("Mobs");
            } else if (entity instanceof ArmorStandEntity) {
                return targetSettings.contains("Armor Stand");
            }
            return false;
        }
    }
}
