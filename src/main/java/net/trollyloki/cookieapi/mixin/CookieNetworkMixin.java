package net.trollyloki.cookieapi.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.util.Identifier;
import net.trollyloki.cookieapi.CookieNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerCommonNetworkHandler.class)
public class CookieNetworkMixin implements CookieNetworkHandler {

    private final Map<Identifier, Queue<CompletableFuture<byte[]>>> cookieRequests = new HashMap<>();

    @Shadow
    private void sendPacket(Packet<?> packet) {

    }

    @Override
    public CompletableFuture<byte[]> requestCookie(Identifier identifier) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        cookieRequests.computeIfAbsent(identifier, k -> new LinkedList<>()).add(future);
        sendPacket(new CookieRequestS2CPacket(identifier));

        return future;
    }

    @Inject(method = "onCookieResponse(LCookieResponseC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    public void onCookieResponse(CookieResponseC2SPacket packet, CallbackInfo info) {
        Identifier identifier = packet.key();

        if (!cookieRequests.containsKey(identifier)) {
            return;
        }

        Queue<CompletableFuture<byte[]>> queue = cookieRequests.get(identifier);
        queue.remove().complete(packet.payload());
        if (queue.isEmpty()) {
            cookieRequests.remove(identifier);
        }
        info.cancel();
    }

}
