package net.trollyloki.cookiedebugmod.mixin;

import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.trollyloki.cookiedebugmod.CookieDebugMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public class CookieResponseMixin {

    @Shadow
    private ServerPlayerEntity player;

    //@Override
    public void onCookieResponse(CookieResponseC2SPacket packet) {
        CookieDebugMod.onCookieResponse(player, packet.key(), packet.payload());
    }

}
