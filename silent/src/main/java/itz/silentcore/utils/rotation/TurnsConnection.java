package itz.silentcore.utils.rotation;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.PacketEvent;
import itz.silentcore.feature.event.impl.PlayerVelocityStrafeEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.math.TaskPriority;
import itz.silentcore.utils.math.TaskProcessor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
public class TurnsConnection implements IMinecraft {
    public static final TurnsConnection INSTANCE = new TurnsConnection();

    private TurnsConstructor lastRotationPlan;
    private final TaskProcessor<TurnsConstructor> rotationPlanTaskProcessor = new TaskProcessor<>();
    private Turns currentAngle;
    private Turns previousAngle;
    private Turns serverAngle = Turns.DEFAULT;
    private Turns fakeAngle;

    private TurnsConnection() {
        SilentCore.getInstance().eventBus.register(this);
    }

    public void setRotation(Turns value) {
        if (value == null) {
            this.previousAngle = this.currentAngle != null ? this.currentAngle : MathAngle.cameraAngle();
        } else {
            this.previousAngle = this.currentAngle;
        }
        this.currentAngle = value;
    }

    public Turns getRotation() {
        return currentAngle != null ? currentAngle : MathAngle.cameraAngle();
    }

    public Turns getFakeRotation() {
        if (fakeAngle != null) {
            return fakeAngle;
        }
        return currentAngle != null ? currentAngle : previousAngle != null ? previousAngle : MathAngle.cameraAngle();
    }

    public void setFakeRotation(Turns angle) {
        this.fakeAngle = angle;
    }

    public Turns getPreviousRotation() {
        return currentAngle != null && previousAngle != null ? previousAngle 
                : new Turns(mc.player.prevYaw, mc.player.prevPitch);
    }

    public Turns getMoveRotation() {
        TurnsConstructor rotationPlan = getCurrentRotationPlan();
        return currentAngle != null && rotationPlan != null && rotationPlan.isMoveCorrection() 
                ? currentAngle : MathAngle.cameraAngle();
    }

    public TurnsConstructor getCurrentRotationPlan() {
        return rotationPlanTaskProcessor.fetchActiveTaskValue() != null 
                ? rotationPlanTaskProcessor.fetchActiveTaskValue() : lastRotationPlan;
    }

    public void rotateTo(Turns.VecRotation vecRotation, LivingEntity entity, int reset, 
                        TurnsConfig config, TaskPriority taskPriority, Module provider) {
        rotateTo(config.createRotationPlan(vecRotation.getAngle(), vecRotation.getVec(), entity, reset), 
                taskPriority, provider);
    }

    public void rotateTo(Turns angle, int reset, TurnsConfig config, TaskPriority taskPriority, Module provider) {
        rotateTo(config.createRotationPlan(angle, angle.toVector(), null, reset), taskPriority, provider);
    }

    public void rotateTo(Turns angle, TurnsConfig config, TaskPriority taskPriority, Module provider) {
        rotateTo(config.createRotationPlan(angle, angle.toVector(), null, 1), taskPriority, provider);
    }

    public void rotateTo(TurnsConstructor plan, TaskPriority taskPriority, Module provider) {
        rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<>(1, taskPriority.getPriority(), provider, plan));
    }

    public void update() {
        TurnsConstructor activePlan = getCurrentRotationPlan();
        if (activePlan == null) {
            setRotation(null);
            return;
        }

        Turns clientAngle = MathAngle.cameraAngle();

        if (lastRotationPlan != null) {
            double differenceFromCurrentToPlayer = computeRotationDifference(serverAngle, clientAngle);
            if (activePlan.getTicksUntilReset() <= rotationPlanTaskProcessor.tickCounter 
                    && differenceFromCurrentToPlayer < activePlan.getResetThreshold()) {
                setRotation(null);
                lastRotationPlan = null;
                rotationPlanTaskProcessor.tickCounter = 0;
                return;
            }
        }

        Turns newAngle = activePlan.nextRotation(
                currentAngle != null ? currentAngle : clientAngle, 
                rotationPlanTaskProcessor.fetchActiveTaskValue() == null
        ).adjustSensitivity(currentAngle != null ? currentAngle : serverAngle);

        setRotation(newAngle);
        lastRotationPlan = activePlan;
        rotationPlanTaskProcessor.tick(1);
    }

    public static double computeRotationDifference(Turns a, Turns b) {
        return Math.hypot(
                Math.abs(computeAngleDifference(a.getYaw(), b.getYaw())), 
                Math.abs(a.getPitch() - b.getPitch())
        );
    }

    public static float computeAngleDifference(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    public void clear() {
        rotationPlanTaskProcessor.activeTasks.clear();
        currentAngle = null;
        previousAngle = null;
        lastRotationPlan = null;
        rotationPlanTaskProcessor.tickCounter = 0;
    }
    
    /**
     * Коррекция движения при ротации (Focused режим)
     */
    @Subscribe
    public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent e) {
        TurnsConstructor currentRotationPlan = getCurrentRotationPlan();
        if (currentRotationPlan != null && currentRotationPlan.isMoveCorrection()) {
            e.setVelocity(fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
        }
    }
    
    /**
     * Исправление velocity с учетом текущей ротации
     */
    private Vec3d fixVelocity(Vec3d currVelocity, Vec3d movementInput, float speed) {
        if (currentAngle != null) {
            float yaw = currentAngle.getYaw();
            double d = movementInput.lengthSquared();

            if (d < 1.0E-7) {
                return Vec3d.ZERO;
            } else {
                Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);

                float f = MathHelper.sin(yaw * 0.017453292f);
                float g = MathHelper.cos(yaw * 0.017453292f);

                return new Vec3d(vec3d.getX() * g - vec3d.getZ() * f, vec3d.getY(), vec3d.getZ() * g + vec3d.getX() * f);
            }
        }
        return currVelocity;
    }

    @Subscribe
    public void onTick(TickEvent e) {
        if (e.isPre()) {
            update();
        }
    }

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (!event.isCancelled() && event.isSend()) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                if (packet.changesLook()) {
                    if (currentAngle != null) {
                        serverAngle = currentAngle;
                    } else {
                        serverAngle = new Turns(packet.getYaw(1), packet.getPitch(1));
                    }
                }
            }
        }
    }
}
