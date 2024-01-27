package net.trollyloki.cookieapi.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.trollyloki.cookieapi.CookiePlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class CookiePlayNetworkMixin extends ServerCommonNetworkHandler implements CookiePlayNetworkHandler {

    private CookiePlayNetworkMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void storeCookie(Identifier identifier, byte[] payload) {
        if (payload.length > StoreCookieS2CPacket.MAX_COOKIE_LENGTH) {
            throw new IllegalArgumentException("Payload too long (%d > %d)"
                    .formatted(payload.length, StoreCookieS2CPacket.MAX_COOKIE_LENGTH));
        }
        sendPacket(new StoreCookieS2CPacket(identifier, payload));
    }

}
