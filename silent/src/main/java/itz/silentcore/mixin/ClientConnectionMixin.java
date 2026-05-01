package itz.silentcore.mixin;

import io.netty.channel.ChannelHandlerContext;
import itz.silentcore.feature.event.impl.PacketEvent;
import itz.silentcore.utils.rotation.Turns;
import itz.silentcore.utils.rotation.TurnsConnection;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    
    private static final ThreadLocal<Boolean> SENDING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // Предотвращаем рекурсию
        if (SENDING.get()) {
            return;
        }
        
        try {
            SENDING.set(true);
            
            // Применяем ротацию к пакетам движения
            if (packet instanceof PlayerMoveC2SPacket movePacket) {
                Turns rotation = TurnsConnection.INSTANCE.getCurrentAngle();
                if (rotation != null && movePacket.changesLook()) {
                    PlayerMoveC2SPacket newPacket;
                    if (movePacket.changesPosition()) {
                        newPacket = new PlayerMoveC2SPacket.Full(
                                movePacket.getX(0), 
                                movePacket.getY(0), 
                                movePacket.getZ(0),
                                rotation.getYaw(), 
                                rotation.getPitch(),
                                movePacket.isOnGround(),
                                movePacket.horizontalCollision()
                        );
                    } else {
                        newPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                                rotation.getYaw(), 
                                rotation.getPitch(),
                                movePacket.isOnGround(),
                                movePacket.horizontalCollision()
                        );
                    }
                    
                    // Отправляем измененный пакет
                    PacketEvent event = new PacketEvent(newPacket, true);
                    event.hook();
                    
                    if (!event.isCancelled()) {
                        ((ClientConnection)(Object)this).send(newPacket);
                    }
                    ci.cancel();
                    return;
                }
            }
            
            // Обычная обработка пакетов
            PacketEvent event = new PacketEvent(packet, true);
            event.hook();
            
            if (event.isCancelled()) {
                ci.cancel();
            }
        } finally {
            SENDING.set(false);
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent event = new PacketEvent(packet, false);
        event.hook();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
