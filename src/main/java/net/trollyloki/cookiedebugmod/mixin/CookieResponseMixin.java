package net.trollyloki.cookiedebugmod.mixin;

import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.trollyloki.cookiedebugmod.CookieDebugMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.charset.StandardCharsets;

@Mixin(ServerPlayNetworkHandler.class)
public class CookieResponseMixin {

    @Shadow
    private ServerPlayerEntity player;

    //@Override
    public void onCookieResponse(CookieResponseC2SPacket packet) {
        Identifier identifier = packet.key();
        byte[] payload = packet.payload();
        String string = payload == null ? null : new String(payload, StandardCharsets.UTF_8);
        CookieDebugMod.LOGGER.info("Cookie received from " + player.getName() + ": " + identifier + " = " + string);
    }

}
