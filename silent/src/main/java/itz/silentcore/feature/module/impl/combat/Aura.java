package itz.silentcore.feature.module.impl.combat;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.PacketEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.combat.MultiPoint;
import itz.silentcore.utils.combat.StrikeManager;
import itz.silentcore.utils.combat.TargetFinder;
import itz.silentcore.utils.rotation.*;
import itz.silentcore.utils.rotation.impl.*;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

@ModuleAnnotation(name = "Aura", category = Category.COMBAT, description = "Автоматическая атака ближайших целей")
public class Aura extends Module {
    private static Aura instance;
    private static final float RANGE_MARGIN = 0.253F;
    
    public static Aura getInstance() {
        return instance;
    }
    
    public Aura() {
        instance = this;
    }

    // Настройки наводки
    private final ModeSetting aimMode = new ModeSetting("Наводка", 
            "FunTime", "Legit Snap", "ReallyWorld", "HolyWorld", "SpookyTime", 
            "CakeWorld", "Matrix", "HvH", "Legit", "Snap");
    
    // Настройки целей
    private final MultiBooleanSetting targetType = new MultiBooleanSetting("Тип таргета",
            MultiBooleanSetting.Value.of("Players"),
            MultiBooleanSetting.Value.of("Mobs"),
            MultiBooleanSetting.Value.of("Animals"),
            MultiBooleanSetting.Value.of("Friends"),
            MultiBooleanSetting.Value.of("Armor Stand"));
    
    // Настройки дистанции
    private final NumberSetting attackRange = new NumberSetting("Дистанция атаки", 3.0f, 1.0f, 6.0f, 0.1f);
    private final NumberSetting lookRange = new NumberSetting("Доп. дистанция поиска", 1.5f, 0.0f, 2.0f, 0.1f);
    
    // Настройки атаки
    private final MultiBooleanSetting attackSettings = new MultiBooleanSetting("Настройки атаки",
            MultiBooleanSetting.Value.of("Only Critical", true),
            MultiBooleanSetting.Value.of("Break Shield", false),
            MultiBooleanSetting.Value.of("UnPress Shield", false),
            MultiBooleanSetting.Value.of("No Attack When Eat", false),
            MultiBooleanSetting.Value.of("Ignore The Walls", true),
            MultiBooleanSetting.Value.of("Hit Chance", false));
    
    private final NumberSetting hitChance = new NumberSetting("Шанс удара %", 100f, 1f, 100f, 1f,
            () -> attackSettings.isEnable("Hit Chance"));
    
    // Настройки коррекции
    private final ModeSetting correctionType = new ModeSetting("Коррекция движения", 
            "Free", "Focused", "Not visible");
    
    private final ModeSetting sprintReset = new ModeSetting("Сброс спринта", "Legit", "Packet");
    
    private final BooleanSetting smartCrits = new BooleanSetting("Умные криты", true,
            () -> attackSettings.isEnable("Only Critical"));

    // Системы
    private final TargetFinder targetSelector = new TargetFinder();
    private final MultiPoint pointFinder = new MultiPoint();
    private final StrikeManager strikeManager = new StrikeManager();

    @Getter
    private LivingEntity target;
    private LivingEntity lastTarget;
    
    // Дополнительные поля
    private Box targetBox;
    private boolean shieldBroken = false;

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof EntityStatusS2CPacket status && status.getStatus() == 30) {
            Entity entity = status.getEntity(mc.world);
            if (entity != null && entity.equals(target)) {
                shieldBroken = true;
                // Щит сломан
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        if (event.isPre()) {
            // PRE - поиск цели и ротация
            target = updateTarget();
            
            if (target != null) {
                rotateToTarget();
                lastTarget = target;
            } else {
                TurnsConnection.INSTANCE.clear();
            }
        } else {
            // POST - атака через StrikeManager
            if (target != null) {
                StrikeManager.AttackConfig config = getConfig();
                strikeManager.handleAttack(config);
            }
        }
    }

    /**
     * Обновление текущей цели
     */
    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(targetType.getSelectedNames());
        float range = attackRange.getCurrent() + RANGE_MARGIN + lookRange.getCurrent();
        float fov = 360; // Полный обзор

        targetSelector.searchTargets(mc.world.getEntities(), range, fov, 
                attackSettings.isEnable("Ignore The Walls"));
        targetSelector.validateTarget(filter::isValid);
        
        return targetSelector.getCurrentTarget();
    }

    /**
     * Ротация к цели с учетом выбранного режима
     */
    private void rotateToTarget() {
        if (target == null) return;
        
        // Вычисляем точку атаки с учетом хитбокса
        float baseRange = attackRange.getCurrent() + RANGE_MARGIN;
        Vec3d attackPoint = pointFinder.computeVector(
                target,
                baseRange,
                TurnsConnection.INSTANCE.getRotation(),
                (float) getSmoothMode().randomValue().x,
                attackSettings.isEnable("Ignore The Walls")
        );

        // Вычисляем угол к цели
        Turns angle = MathAngle.fromVec3d(attackPoint.subtract(Objects.requireNonNull(mc.player).getEyePos()));
        Turns.VecRotation rotation = new Turns.VecRotation(angle, attackPoint);
        
        // Создаем конфигурацию ротации
        TurnsConfig config = getRotationConfig();
        
        // Применяем ротацию с учетом режима и приоритета
        int smoothness = getRotationSmoothness();
        TurnsConnection.INSTANCE.rotateTo(rotation, target, smoothness, config, 
                itz.silentcore.utils.math.TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    /**
     * Получение конфигурации ротации
     */
    private TurnsConfig getRotationConfig() {
        boolean visibleCorrection = !correctionType.get().equals("Not visible");
        boolean freeCorrection = correctionType.get().equals("Free");
        
        // Focused режим - отключаем свободную коррекцию для вжимания в цель
        if (correctionType.get().equals("Focused")) {
            freeCorrection = false;
        }
        
        return new TurnsConfig(getSmoothMode(), visibleCorrection, freeCorrection);
    }

    /**
     * Получение плавности ротации в зависимости от режима
     */
    private int getRotationSmoothness() {
        return switch (aimMode.get()) {
            case "FunTime" -> 40;
            case "HolyWorld" -> 10;
            case "Legit Snap" -> 1;
            case "Matrix" -> 5;
            case "HvH" -> 1;
            default -> 1; // ReallyWorld, SpookyTime, CakeWorld, Snap, Legit
        };
    }

    /**
     * Получение режима ротации
     */
    private RotateConstructor getSmoothMode() {
        return switch (aimMode.get()) {
            case "FunTime" -> new FTAngle();
            case "HolyWorld" -> new HWAngle();
            case "HvH" -> new HAngle();
            case "CakeWorld" -> new LGAngle();
            case "SpookyTime" -> new SPAngle();
            case "ReallyWorld" -> new RWAngle();
            case "Snap", "Legit Snap" -> new SnapAngle();
            case "Matrix" -> new MatrixAngle();
            case "Legit" -> new LegitAngle();
            default -> new LinearConstructor();
        };
    }

    /**
     * Получение конфигурации для атаки
     */
    public StrikeManager.AttackConfig getConfig() {
        float baseRange = attackRange.getCurrent() + RANGE_MARGIN;
        
        Vec3d computedPoint = pointFinder.computeVector(
                target,
                baseRange,
                TurnsConnection.INSTANCE.getRotation(),
                (float) getSmoothMode().randomValue().x,
                attackSettings.isEnable("Ignore The Walls")
        );

        Turns angle = MathAngle.fromVec3d(computedPoint.subtract(Objects.requireNonNull(mc.player).getEyePos()));

        return new StrikeManager.AttackConfig(
                target,
                baseRange,
                attackSettings.getSelectedNames(),
                hitChance.getCurrent(),
                sprintReset.get(),
                smartCrits.isEnabled()
        );
    }

    /**
     * Проверка, является ли цель валидной для атаки
     */
    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null) return false;
        if (entity == mc.player) return false;
        if (entity.isRemoved()) return false;
        if (!entity.isAlive()) return false;
        if (entity.getHealth() <= 0) return false;
        
        return true;
    }

    /**
     * Получение приоритета цели (чем меньше, тем выше приоритет)
     */
    private float getTargetPriority(LivingEntity entity) {
        float priority = 0;
        
        // Приоритет по дистанции
        priority += mc.player.distanceTo(entity);
        
        // Приоритет по здоровью (меньше здоровья = выше приоритет)
        priority -= (20 - entity.getHealth()) * 0.5f;
        
        // Приоритет игрокам
        if (entity instanceof PlayerEntity) {
            priority -= 5;
        }
        
        return priority;
    }

    /**
     * Проверка, находится ли цель в FOV
     */
    private boolean isInFOV(LivingEntity entity, float fov) {
        if (fov >= 360) return true;
        
        Vec3d toEntity = entity.getPos().subtract(mc.player.getPos()).normalize();
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        
        double dot = toEntity.dotProduct(lookVec);
        double angle = Math.toDegrees(Math.acos(dot));
        
        return angle <= fov / 2.0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        shieldBroken = false;
    }

    @Override
    public void onDisable() {
        targetSelector.releaseTarget();
        target = null;
        lastTarget = null;
        shieldBroken = false;
        TurnsConnection.INSTANCE.clear();
        super.onDisable();
    }
}
