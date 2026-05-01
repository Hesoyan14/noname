package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ColorSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorUtils;
import itz.silentcore.utils.render.Render3D;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@ModuleAnnotation(name = "WorldParticles", category = Category.RENDER, description = "World particles")
public class WorldParticles extends Module implements IMinecraft {

    private final ModeSetting mode = new ModeSetting("Mode", "2D", "2D", "3D");

    // 2D Settings
    private final ModeSetting particleType = new ModeSetting("Particle Type", "Stars", "Stars", "Snow", "Bloom");
    private final BooleanSetting spawnFromGround = new BooleanSetting("Spawn From Ground", true);
    private final BooleanSetting collision = new BooleanSetting("Collision", true);
    private final BooleanSetting scale = new BooleanSetting("Scale", true);
    private final NumberSetting maxParticles = new NumberSetting("Max Particles", 50f, 10f, 200f, 1f);
    private final NumberSetting spawnRate = new NumberSetting("Spawn Rate", 15f, 10f, 200f, 1f);

    private final NumberSetting spawnHeight = new NumberSetting("Spawn Height", 10f, 0.05f, 30f, 0.5f);
    private final NumberSetting particleGravity = new NumberSetting("Gravity", 0f, -10f, 10f, 0.1f);
    private final NumberSetting motionPower = new NumberSetting("Motion Power", 1f, 0.1f, 2f, 0.1f);
    private final NumberSetting inclineX = new NumberSetting("Incline X", 0f, -17.5f, 17.5f, 0.5f);
    private final NumberSetting inclineZ = new NumberSetting("Incline Z", 0f, -17.5f, 17.5f, 0.5f);
    private final NumberSetting particleSize = new NumberSetting("Particle Size", 1f, 0.5f, 2f, 0.1f);
    private final NumberSetting lifeTime = new NumberSetting("Life Time", 800f, 250f, 3000f, 50f);
    private final NumberSetting spawnRange = new NumberSetting("Spawn Range", 25f, 10f, 50f, 1f);

    // 3D Settings
    private final NumberSetting particleCount = new NumberSetting("Particle Count", 50f, 10f, 200f, 1f);
    private final NumberSetting range = new NumberSetting("Range", 32f, 8f, 64f, 1f);
    private final NumberSetting size = new NumberSetting("Size", 0.09f, 0.05f, 0.15f, 0.01f);
    
    private final List<WorldCrystal> crystalList = new ArrayList<>();
    private final List<Particle2D> particles = new ArrayList<>();
    private final Random random = new Random();
    private int previousParticleCount;
    private long lastSpawnTime = 0;

    public WorldParticles() {
        particleType.setVisible(() -> mode.get().equals("2D"));
        spawnFromGround.setVisible(() -> mode.get().equals("2D"));
        collision.setVisible(() -> mode.get().equals("2D"));
        scale.setVisible(() -> mode.get().equals("2D"));
        maxParticles.setVisible(() -> mode.get().equals("2D"));
        spawnRate.setVisible(() -> mode.get().equals("2D"));
        spawnHeight.setVisible(() -> mode.get().equals("2D"));
        particleGravity.setVisible(() -> mode.get().equals("2D"));
        motionPower.setVisible(() -> mode.get().equals("2D"));
        inclineX.setVisible(() -> mode.get().equals("2D"));
        inclineZ.setVisible(() -> mode.get().equals("2D"));
        particleSize.setVisible(() -> mode.get().equals("2D"));
        lifeTime.setVisible(() -> mode.get().equals("2D"));
        spawnRange.setVisible(() -> mode.get().equals("2D"));

        particleCount.setVisible(() -> mode.get().equals("3D"));
        range.setVisible(() -> mode.get().equals("3D"));
        size.setVisible(() -> mode.get().equals("3D"));

        previousParticleCount = (int) particleCount.getCurrent();
        lastSpawnTime = System.currentTimeMillis();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mode.get().equals("3D")) {
            generateCrystals();
        }
        previousParticleCount = (int) particleCount.getCurrent();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        crystalList.clear();
        particles.clear();
    }


    @Subscribe
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null) return;

        if (mode.get().equals("3D")) {
            int currentCount = (int) particleCount.getCurrent();
            if (currentCount != previousParticleCount) {
                adjustCrystalCount(currentCount);
                previousParticleCount = currentCount;
            }
            updateCrystals();
            renderCrystals(event.getMatrices());
        } else {
            long now = System.currentTimeMillis();
            double spawnInterval = 1000.0 / spawnRate.getCurrent();
            if (now - lastSpawnTime >= spawnInterval && particles.size() < maxParticles.getCurrent()) {
                spawnParticle();
                lastSpawnTime = now;
            }
            if (particles.size() > maxParticles.getCurrent()) {
                Iterator<Particle2D> it = particles.iterator();
                while (it.hasNext() && particles.size() > maxParticles.getCurrent()) {
                    it.next();
                    it.remove();
                }
            }

            Iterator<Particle2D> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle2D particle = iterator.next();
                particle.update();
                if (particle.isDead()) {
                    iterator.remove();
                }
            }
            renderParticles(event.getMatrices());
        }
    }

    private void adjustCrystalCount(int targetCount) {
        int currentSize = crystalList.size();
        if (targetCount > currentSize) {
            addCrystals(targetCount - currentSize);
        } else if (targetCount < currentSize) {
            markCrystalsForRemoval(currentSize - targetCount);
        }
    }

    private void addCrystals(int count) {
        if (mc.player == null) return;
        Vec3d playerPos = mc.player.getPos();
        float rangeValue = range.getCurrent();
        for (int i = 0; i < count; i++) {
            Vec3d position;
            int attempts = 0;
            do {
                double x = playerPos.x + (random.nextDouble() - 0.5) * 2 * rangeValue;
                double y = playerPos.y + (random.nextDouble() - 0.5) * rangeValue;
                double z = playerPos.z + (random.nextDouble() - 0.5) * 2 * rangeValue;
                position = new Vec3d(x, y, z);
                attempts++;
            } while (!isInPlayerView(position) && attempts < 20);
            Vec3d velocity = new Vec3d(
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02
            );
            Vec3d rotation = new Vec3d(
                    random.nextDouble() * 360,
                    random.nextDouble() * 360,
                    random.nextDouble() * 360
            );
            crystalList.add(new WorldCrystal(position, velocity, rotation));
        }
    }


    private void markCrystalsForRemoval(int count) {
        int marked = 0;
        for (WorldCrystal crystal : crystalList) {
            if (marked >= count) break;
            if (!crystal.markedForDeath && !crystal.isFadingOut) {
                crystal.markedForDeath = true;
                crystal.isFadingOut = true;
                marked++;
            }
        }
    }

    private void generateCrystals() {
        crystalList.clear();
        if (mc.player == null) return;
        Vec3d playerPos = mc.player.getPos();
        int count = (int) particleCount.getCurrent();
        float rangeValue = range.getCurrent();
        for (int i = 0; i < count; i++) {
            Vec3d position;
            int attempts = 0;
            do {
                double x = playerPos.x + (random.nextDouble() - 0.5) * 2 * rangeValue;
                double y = playerPos.y + (random.nextDouble() - 0.5) * rangeValue;
                double z = playerPos.z + (random.nextDouble() - 0.5) * 2 * rangeValue;
                position = new Vec3d(x, y, z);
                attempts++;
            } while (!isInPlayerView(position) && attempts < 20);
            Vec3d velocity = new Vec3d(
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02
            );
            Vec3d rotation = new Vec3d(
                    random.nextDouble() * 360,
                    random.nextDouble() * 360,
                    random.nextDouble() * 360
            );
            crystalList.add(new WorldCrystal(position, velocity, rotation));
        }
    }

    private boolean isBlockOccluding(Vec3d crystalPos) {
        if (mc.world == null || mc.player == null) return false;
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d direction = crystalPos.subtract(cameraPos).normalize();
        double distance = crystalPos.distanceTo(cameraPos);
        double step = 0.5;
        for (double d = 0; d < distance; d += step) {
            Vec3d checkPos = cameraPos.add(direction.multiply(d));
            BlockPos blockPos = BlockPos.ofFloored(checkPos);
            if (!mc.world.getBlockState(blockPos).isAir()) {
                return true;
            }
        }
        return false;
    }


    private void updateCrystals() {
        if (mc.player == null) return;
        Vec3d playerPos = mc.player.getPos();
        float rangeValue = range.getCurrent();
        float fadeSpeedValue = 0.05f;

        Iterator<WorldCrystal> iterator = crystalList.iterator();
        while (iterator.hasNext()) {
            WorldCrystal crystal = iterator.next();
            crystal.prevPosition = crystal.position;
            crystal.position = crystal.position.add(crystal.velocity);

            boolean isOccluded = isBlockOccluding(crystal.position);
            boolean inView = isInPlayerView(crystal.position);

            if (crystal.markedForDeath) {
                crystal.fadeAlpha -= fadeSpeedValue;
                if (crystal.fadeAlpha <= 0) {
                    iterator.remove();
                    continue;
                }
            } else {
                if (isOccluded || !inView) {
                    if (!crystal.isFadingOut) {
                        crystal.isFadingOut = true;
                    }
                } else {
                    if (crystal.isFadingOut) {
                        crystal.isFadingOut = false;
                    }
                }

                if (crystal.isFadingOut) {
                    crystal.fadeAlpha -= fadeSpeedValue;
                    if (crystal.fadeAlpha <= 0) {
                        crystal.fadeAlpha = 0;
                        Vec3d newPosition;
                        int attempts = 0;
                        do {
                            double x = playerPos.x + (random.nextDouble() - 0.5) * 2 * rangeValue;
                            double y = playerPos.y + (random.nextDouble() - 0.5) * rangeValue;
                            double z = playerPos.z + (random.nextDouble() - 0.5) * 2 * rangeValue;
                            newPosition = new Vec3d(x, y, z);
                            attempts++;
                        } while (!isInPlayerView(newPosition) && attempts < 20);
                        crystal.position = newPosition;
                        crystal.prevPosition = crystal.position;
                        crystal.isFadingOut = false;
                    }
                } else {
                    crystal.fadeAlpha += fadeSpeedValue;
                    if (crystal.fadeAlpha > 1.0f) {
                        crystal.fadeAlpha = 1.0f;
                    }
                }

                if (crystal.position.distanceTo(playerPos) > rangeValue * 1.5) {
                    Vec3d newPosition;
                    int attempts = 0;
                    do {
                        double x = playerPos.x + (random.nextDouble() - 0.5) * 2 * rangeValue;
                        double y = playerPos.y + (random.nextDouble() - 0.5) * rangeValue;
                        double z = playerPos.z + (random.nextDouble() - 0.5) * 2 * rangeValue;
                        newPosition = new Vec3d(x, y, z);
                        attempts++;
                    } while (!isInPlayerView(newPosition) && attempts < 20);
                    crystal.position = newPosition;
                    crystal.prevPosition = crystal.position;
                    crystal.fadeAlpha = 0;
                    crystal.isFadingOut = false;
                }
            }
        }
    }


    private float getCameraYaw() {
        Camera camera = mc.gameRenderer.getCamera();
        return camera.getYaw();
    }

    private Vec3d getCameraLookVec() {
        Camera camera = mc.gameRenderer.getCamera();
        return Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
    }

    private boolean isInPlayerView(Vec3d pos) {
        if (mc.gameRenderer.getCamera() == null) return true;
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos = cam.getPos();
        Vec3d look = getCameraLookVec();
        Vec3d toParticle = pos.subtract(camPos).normalize();
        return look.dotProduct(toParticle) > 0.1;
    }

    private void renderCrystals(MatrixStack ms) {
        if (mc.player == null || crystalList.isEmpty()) return;
        Camera camera = mc.gameRenderer.getCamera();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        for (WorldCrystal crystal : crystalList) {
            if (crystal.fadeAlpha <= 0) continue;

            float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
            Vec3d renderPos = crystal.prevPosition.lerp(crystal.position, tickDelta);

            if (!isInPlayerView(renderPos) && !crystal.isFadingOut) continue;

            ms.push();
            ms.translate(renderPos.x, renderPos.y, renderPos.z);
            float pulsation = 1.0f + (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.1f);
            ms.scale(pulsation, pulsation, pulsation);
            float selfRotation = (System.currentTimeMillis() % 36000) / 100.0f * crystal.rotationSpeed;
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) crystal.rotation.x));
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) crystal.rotation.y + selfRotation));
            ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) crystal.rotation.z));
            crystal.render(ms, itz.silentcore.feature.theme.ThemeManager.getInstance().getPrimaryColor(), camera, size.getCurrent(), 8);
            ms.pop();
        }
    }

    private Vec3d getRandomMotion() {
        return new Vec3d(
                (random.nextDouble() - 0.5) * 0.08,
                random.nextDouble() * 0.05,
                (random.nextDouble() - 0.5) * 0.08
        );
    }


    private void spawnParticle() {
        if (mc.player == null || mc.world == null) return;
        double value = spawnRange.getCurrent();
        double offsetX = (random.nextDouble() - 0.5) * 2 * value;
        double offsetZ = (random.nextDouble() - 0.5) * 2 * value;
        Vec3d additional = mc.player.getPos().add(offsetX, 0, offsetZ);
        BlockPos bpos;
        if (spawnFromGround.isEnabled()) {
            bpos = mc.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(additional));
        } else {
            double offsetY = random.nextDouble() * spawnHeight.getCurrent();
            bpos = BlockPos.ofFloored(mc.player.getPos().add(offsetX, offsetY, offsetZ));
        }
        Vec3d pos = new Vec3d(bpos.getX() + 0.5, bpos.getY(), bpos.getZ() + 0.5);
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (!mc.world.getBlockState(blockPos).isAir() || !isInPlayerView(pos)) {
            return;
        }
        Vec3d vel = getRandomMotion().multiply(motionPower.getCurrent());
        int lifetime = (int) lifeTime.getCurrent() + random.nextInt(50) - 25;
        lifetime = Math.max(150, lifetime);
        int color = itz.silentcore.feature.theme.ThemeManager.getInstance().getPrimaryColor();
        particles.add(new Particle2D(pos, vel, lifetime, color));
    }

    private void renderParticles(MatrixStack stack) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);

        String texturePath = switch (particleType.get()) {
            case "Snow" -> "silentcore:textures/particles/show1.png";
            case "Bloom" -> "silentcore:textures/particles/glow.png";
            default -> "silentcore:textures/particles/star1.png";
        };
        Identifier textureId = Identifier.of(texturePath);

        for (Particle2D particle : particles) {
            float alpha = particle.fade();
            if (alpha <= 0) continue;

            if (isBlockOccluding(particle.pos)) continue;

            float scaleFactor = particle.scaleFactor();
            Color baseColor = new Color(particle.colorInt);
            int r = baseColor.getRed();
            int g = baseColor.getGreen();
            int b = baseColor.getBlue();
            float finalAlpha = alpha;
            int a = (int) (finalAlpha * 255);

            int argb = new Color(r, g, b, a).getRGB();

            double posX = particle.pos.x - camPos.x;
            double posY = particle.pos.y - camPos.y;
            double posZ = particle.pos.z - camPos.z;

            MatrixStack matrices = new MatrixStack();
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(posX, posY, posZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            float baseScale = particleSize.getCurrent();
            float finalScale = baseScale * scaleFactor;

            Render3D.drawTexture(
                    matrices.peek(),
                    textureId,
                    -finalScale / 2,
                    -finalScale / 2,
                    finalScale,
                    finalScale,
                    new org.joml.Vector4i(argb),
                    false
            );
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();
    }


    private class WorldCrystal {
        Vec3d position;
        Vec3d prevPosition;
        final Vec3d velocity;
        final Vec3d rotation;
        final float rotationSpeed;
        float fadeAlpha;
        boolean isFadingOut;
        boolean markedForDeath;

        public WorldCrystal(Vec3d position, Vec3d velocity, Vec3d rotation) {
            this.position = position;
            this.prevPosition = position;
            this.velocity = velocity;
            this.rotation = rotation;
            this.rotationSpeed = 0.5f + (float)(Math.random() * 1.5f);
            this.fadeAlpha = 0.0f;
            this.isFadingOut = false;
            this.markedForDeath = false;
        }

        public void render(MatrixStack ms, int baseColor, Camera camera, float size, float bloomSizeMultiplier) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            drawCrystal(ms, baseColor, 0.2f, true, size);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            drawCrystal(ms, baseColor, 0.3f, true, size);
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            ms.push();
            ms.scale(1.2f, 1.2f, 1.2f);
            drawCrystal(ms, baseColor, 0.3f, true, size);
            ms.pop();
            drawBloomSphere(ms, baseColor, camera, size, bloomSizeMultiplier);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        }

        private void drawBloomSphere(MatrixStack ms, int baseColor, Camera camera, float size, float bloomSizeMultiplier) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, Identifier.of("silentcore:textures/particles/bloom.png"));
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.depthMask(false);
            int bloomColor = ColorUtils.setAlpha(baseColor, (int)(15 * fadeAlpha));
            float bloomSize = size * bloomSizeMultiplier;
            float pitch = camera.getPitch();
            float yaw = camera.getYaw();
            int segments = 8;
            for (int i = 0; i < segments; i++) {
                ms.push();
                float angle = (360.0f / segments) * i;
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
                Matrix4f matrix = ms.peek().getPositionMatrix();
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferBuilder.vertex(matrix, -bloomSize / 2, -bloomSize / 2, 0).texture(0, 1).color(bloomColor);
                bufferBuilder.vertex(matrix, bloomSize / 2, -bloomSize / 2, 0).texture(1, 1).color(bloomColor);
                bufferBuilder.vertex(matrix, bloomSize / 2, bloomSize / 2, 0).texture(1, 0).color(bloomColor);
                bufferBuilder.vertex(matrix, -bloomSize / 2, bloomSize / 2, 0).texture(0, 0).color(bloomColor);
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                ms.pop();
            }
            for (int i = 0; i < segments; i++) {
                ms.push();
                float angle = (360.0f / segments) * i;
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
                Matrix4f matrix = ms.peek().getPositionMatrix();
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferBuilder.vertex(matrix, -bloomSize / 2, -bloomSize / 2, 0).texture(0, 1).color(bloomColor);
                bufferBuilder.vertex(matrix, bloomSize / 2, -bloomSize / 2, 0).texture(1, 1).color(bloomColor);
                bufferBuilder.vertex(matrix, bloomSize / 2, bloomSize / 2, 0).texture(1, 0).color(bloomColor);
                bufferBuilder.vertex(matrix, -bloomSize / 2, bloomSize / 2, 0).texture(0, 0).color(bloomColor);
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                ms.pop();
            }
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        }


        private void drawCrystal(MatrixStack ms, int baseColor, float alpha, boolean filled, float size) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(
                    filled ? VertexFormat.DrawMode.TRIANGLES : VertexFormat.DrawMode.DEBUG_LINES,
                    VertexFormats.POSITION_COLOR
            );
            float s = size;
            float h_prism = size * 1f;
            float h_pyramid = size * 1.5f;
            int numSides = 8;
            List<Vec3d> topVertices = new ArrayList<>();
            List<Vec3d> bottomVertices = new ArrayList<>();
            for (int i = 0; i < numSides; i++) {
                float angle = (float) (2 * Math.PI * i / numSides);
                float x = (float) (s * Math.cos(angle));
                float z = (float) (s * Math.sin(angle));
                topVertices.add(new Vec3d(x, h_prism / 2, z));
                bottomVertices.add(new Vec3d(x, -h_prism / 2, z));
            }
            Vec3d vTop = new Vec3d(0, h_prism / 2 + h_pyramid, 0);
            Vec3d vBottom = new Vec3d(0, -h_prism / 2 - h_pyramid, 0);
            int finalColor = ColorUtils.setAlpha(baseColor, (int)(55 * fadeAlpha));
            for (int i = 0; i < numSides; i++) {
                Vec3d v1 = bottomVertices.get(i);
                Vec3d v2 = bottomVertices.get((i + 1) % numSides);
                Vec3d v3 = topVertices.get((i + 1) % numSides);
                Vec3d v4 = topVertices.get(i);
                drawQuad(ms, bufferBuilder, v1, v2, v3, v4, finalColor, filled);
            }
            for (int i = 0; i < numSides; i++) {
                Vec3d v1 = topVertices.get(i);
                Vec3d v2 = topVertices.get((i + 1) % numSides);
                drawTriangle(ms, bufferBuilder, vTop, v1, v2, finalColor, filled);
            }
            for (int i = 0; i < numSides; i++) {
                Vec3d v1 = bottomVertices.get(i);
                Vec3d v2 = bottomVertices.get((i + 1) % numSides);
                drawTriangle(ms, bufferBuilder, vBottom, v2, v1, finalColor, filled);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }

        private void drawTriangle(MatrixStack ms, BufferBuilder bb, Vec3d v1, Vec3d v2, Vec3d v3, int color, boolean filled) {
            if (filled) {
                bb.vertex(ms.peek().getPositionMatrix(), (float)v1.x, (float)v1.y, (float)v1.z).color(color);
                bb.vertex(ms.peek().getPositionMatrix(), (float)v2.x, (float)v2.y, (float)v2.z).color(color);
                bb.vertex(ms.peek().getPositionMatrix(), (float)v3.x, (float)v3.y, (float)v3.z).color(color);
            }
        }

        private void drawQuad(MatrixStack ms, BufferBuilder bb, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, int color, boolean filled) {
            if (filled) {
                drawTriangle(ms, bb, v1, v2, v3, color, true);
                drawTriangle(ms, bb, v1, v3, v4, color, true);
            }
        }
    }


    private class Particle2D {
        Vec3d pos;
        Vec3d vel;
        int colorInt;
        boolean hitSurface = false;
        Random rnd = new Random();
        float viewFadeAlpha = 1.0f;
        private final long birthTime;
        private final int totalLife;

        Particle2D(Vec3d pos, Vec3d vel, int life, int colorInt) {
            this.pos = pos;
            this.vel = vel;
            this.colorInt = colorInt;
            this.birthTime = System.currentTimeMillis();
            this.totalLife = 2 * life;
            this.viewFadeAlpha = 0.0f;
        }

        boolean isDead() {
            return System.currentTimeMillis() - birthTime >= totalLife;
        }

        float fade() {
            long ageMs = System.currentTimeMillis() - birthTime;
            float progress = Math.min(1.0f, (float) ageMs / totalLife);
            float fadeInOut;
            if (progress <= 0.5f) {
                fadeInOut = progress * 2.0f;
            } else {
                fadeInOut = 2.0f - progress * 2.0f;
            }
            return fadeInOut * viewFadeAlpha;
        }

        float scaleFactor() {
            return scale.isEnabled() ? fade() : 1.0f;
        }

        void update() {
            if (isDead()) return;

            float fps = mc.getCurrentFps();
            float deltaTime = fps > 0 ? 1.0f / fps : 1.0f / 60.0f;
            float speed = deltaTime / 0.05f;
            float motionMultiplier = motionPower.getCurrent();
            float yaw = getCameraYaw();

            pos = pos.add(vel.multiply(speed * motionMultiplier, speed * motionMultiplier, speed * motionMultiplier));

            double xY = Math.sin(Math.toRadians(yaw));
            double zY = -Math.cos(Math.toRadians(yaw));
            double xX = -Math.sin(Math.toRadians(yaw + 90));
            double zX = Math.cos(Math.toRadians(yaw + 90));

            Vec3d addMotion = new Vec3d(
                    xY * inclineZ.getCurrent() / 50 + xX * inclineX.getCurrent() / 50,
                    0,
                    zY * inclineZ.getCurrent() / 50 + zX * inclineX.getCurrent() / 50
            );

            vel = vel.add(
                    addMotion.x * deltaTime * motionMultiplier,
                    (particleGravity.getCurrent() / 80) * deltaTime * motionMultiplier,
                    addMotion.z * deltaTime * motionMultiplier
            );

            pos = pos.add(vel.multiply(speed, speed, speed));
            vel = vel.add(0, -0.0002, 0);

            if (collision.isEnabled()) {
                BlockPos bp = BlockPos.ofFloored(pos);
                if (!mc.world.getBlockState(bp).isAir()) {
                    Vec3d normal = new Vec3d(0, 1, 0);
                    double dotProduct = vel.dotProduct(normal);
                    Vec3d reflection = vel.subtract(normal.multiply(2 * dotProduct));
                    vel = reflection.multiply(0.8);
                }
            }

            boolean inView = isInPlayerView(pos);
            if (inView) {
                viewFadeAlpha += deltaTime * 2.0f;
                if (viewFadeAlpha > 1.0f) viewFadeAlpha = 1.0f;
            } else {
                viewFadeAlpha -= deltaTime * 1.0f;
                if (viewFadeAlpha < 0) viewFadeAlpha = 0;
            }
        }
    }
}
