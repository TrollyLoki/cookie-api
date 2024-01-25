package net.trollyloki.cookiedebugmod.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.trollyloki.cookiedebugmod.CookieDebugMod;
import net.trollyloki.cookiedebugmod.CookieNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class CookieNetworkMixin extends ServerCommonNetworkHandler implements CookieNetworkHandler {

    private CookieNetworkMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Shadow
    private ServerPlayerEntity player;

    private final Map<Identifier, Queue<CompletableFuture<byte[]>>> cookieRequests = new HashMap<>();

    @Override
    public void storeCookie(Identifier identifier, byte[] payload) {
        if (payload.length > StoreCookieS2CPacket.MAX_COOKIE_LENGTH) {
            throw new IllegalArgumentException("Payload too long (%d > %d)"
                    .formatted(payload.length, StoreCookieS2CPacket.MAX_COOKIE_LENGTH));
        }
        sendPacket(new StoreCookieS2CPacket(identifier, payload));
    }

    @Override
    public CompletableFuture<byte[]> requestCookie(Identifier identifier) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        cookieRequests.computeIfAbsent(identifier, k -> new LinkedList<>()).add(future);
        sendPacket(new CookieRequestS2CPacket(identifier));

        return future;
    }

    @Override
    public void onCookieResponse(CookieResponseC2SPacket packet) {
        Identifier identifier = packet.key();

        if (!cookieRequests.containsKey(identifier)) {
            CookieDebugMod.LOGGER.warn("Unexpected cookie response packet received from " + player.getGameProfile().getName());
            return;
        }

        Queue<CompletableFuture<byte[]>> queue = cookieRequests.get(identifier);
        queue.remove().complete(packet.payload());
        if (queue.isEmpty()) {
            cookieRequests.remove(identifier);
        }
    }

}
