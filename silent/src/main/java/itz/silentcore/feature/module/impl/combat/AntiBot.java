package itz.silentcore.feature.module.impl.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import itz.silentcore.feature.event.impl.PacketEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@ModuleAnnotation(name = "AntiBot", category = Category.COMBAT, description = "Определяет и игнорирует ботов")
public class AntiBot extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "ReallyWorld", "Matrix");
    
    private final Set<UUID> suspectSet = new HashSet<>();
    private static final Set<UUID> botSet = new HashSet<>();

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof PlayerListS2CPacket listPacket) {
                checkPlayerAfterSpawn(listPacket);
            } else if (event.getPacket() instanceof PlayerRemoveS2CPacket removePacket) {
                removePlayerBecauseLeftServer(removePacket);
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Проверяем подозрительных игроков
        if (!suspectSet.isEmpty()) {
            mc.world.getPlayers().stream()
                    .filter(p -> suspectSet.contains(p.getUuid()))
                    .forEach(this::evaluateSuspectPlayer);
        }
        
        // Применяем режим обнаружения
        if (mode.is("Matrix")) {
            matrixMode();
        } else if (mode.is("ReallyWorld")) {
            reallyWorldMode();
        }
    }

    private void checkPlayerAfterSpawn(PlayerListS2CPacket packet) {
        packet.getPlayerAdditionEntries().forEach(entry -> {
            GameProfile profile = entry.profile();
            if (profile == null || isRealPlayer(entry, profile)) {
                return;
            }
            if (isDuplicateProfile(profile)) {
                botSet.add(profile.getId());
            } else {
                suspectSet.add(profile.getId());
            }
        });
    }

    private void removePlayerBecauseLeftServer(PlayerRemoveS2CPacket packet) {
        packet.profileIds().forEach(uuid -> {
            suspectSet.remove(uuid);
            botSet.remove(uuid);
        });
    }

    private boolean isRealPlayer(PlayerListS2CPacket.Entry entry, GameProfile profile) {
        return entry.latency() < 2 || (profile.getProperties() != null && !profile.getProperties().isEmpty());
    }

    private void evaluateSuspectPlayer(PlayerEntity player) {
        Iterable<ItemStack> armor = player.getArmorItems();
        if (isFullyEquipped(player) || hasArmorChanged(player, armor)) {
            botSet.add(player.getUuid());
        }
        suspectSet.remove(player.getUuid());
    }

    private void matrixMode() {
        Iterator<UUID> iterator = suspectSet.iterator();
        while (iterator.hasNext()) {
            UUID susPlayer = iterator.next();
            PlayerEntity entity = mc.world.getPlayerByUuid(susPlayer);
            if (entity != null) {
                String playerName = entity.getName().getString();
                boolean isNameBot = playerName.startsWith("CIT-") 
                        && !playerName.contains("NPC") 
                        && !playerName.contains("[ZNPC]");
                
                int armorCount = 0;
                for (ItemStack item : entity.getArmorItems()) {
                    if (!item.isEmpty()) armorCount++;
                }
                boolean isFullArmor = armorCount == 4;
                
                boolean isFakeUUID = !entity.getUuid().equals(
                        UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes())
                );
                
                if (isFullArmor || isNameBot || isFakeUUID) {
                    botSet.add(susPlayer);
                }
            }
            iterator.remove();
        }
        
        // Очистка старых ботов
        if (mc.player.age % 100 == 0) {
            botSet.removeIf(uuid -> mc.world.getPlayerByUuid(uuid) == null);
        }
    }

    private void reallyWorldMode() {
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            
            String playerName = entity.getName().getString();
            UUID expectedOfflineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
            boolean hasOnlineUUID = !entity.getUuid().equals(expectedOfflineUUID);
            boolean isNPC = playerName.contains("NPC") || playerName.startsWith("[ZNPC]");
            
            if (hasOnlineUUID && !isNPC && !botSet.contains(entity.getUuid())) {
                botSet.add(entity.getUuid());
            }
        }
    }

    private boolean isDuplicateProfile(GameProfile profile) {
        if (mc.getNetworkHandler() == null) return false;
        return mc.getNetworkHandler().getPlayerList().stream()
                .filter(player -> player.getProfile().getName().equals(profile.getName()) 
                        && !player.getProfile().getId().equals(profile.getId()))
                .count() == 1;
    }

    private boolean isFullyEquipped(PlayerEntity entity) {
        return IntStream.rangeClosed(0, 3)
                .mapToObj(entity.getInventory()::getArmorStack)
                .allMatch(stack -> stack.getItem() instanceof ArmorItem && !stack.hasEnchantments());
    }

    private boolean hasArmorChanged(PlayerEntity entity, Iterable<ItemStack> prevArmor) {
        if (prevArmor == null) return true;
        
        List<ItemStack> currentArmorList = StreamSupport.stream(entity.getArmorItems().spliterator(), false).toList();
        List<ItemStack> prevArmorList = StreamSupport.stream(prevArmor.spliterator(), false).toList();
        
        return !IntStream.range(0, Math.min(currentArmorList.size(), prevArmorList.size()))
                .allMatch(i -> currentArmorList.get(i).equals(prevArmorList.get(i)))
                || currentArmorList.size() != prevArmorList.size();
    }

    public static boolean isBot(PlayerEntity entity) {
        if (entity == null) return false;
        
        String playerName = entity.getName().getString();
        boolean isNameBot = playerName.startsWith("CIT-") 
                && !playerName.contains("NPC") 
                && !playerName.startsWith("[ZNPC]");
        
        return isNameBot || botSet.contains(entity.getUuid());
    }

    public static boolean isBot(UUID uuid) {
        return botSet.contains(uuid);
    }

    @Override
    public void onDisable() {
        suspectSet.clear();
        botSet.clear();
        super.onDisable();
    }
}
