package net.trollyloki.cookieapi.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import net.trollyloki.cookieapi.CookieApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Mixin(ServerLoginNetworkHandler.class)
public class LoginHandlerMixin {

    private static final Identifier VERIFY_COOKIE = new Identifier("custom", "verify");
    private static final byte[] TOP_SECRET_PASSWORD = "WalletTowel20".getBytes(StandardCharsets.UTF_8);

    @Shadow
    private ClientConnection connection;
    @Shadow
    private GameProfile profile;

    private boolean cookieVerified = false;

    @Inject(method = "startVerify(LGameProfile;)V", at = @At("TAIL"))
    private void startVerify(GameProfile gameProfile, CallbackInfo info) {
        connection.send(new CookieRequestS2CPacket(VERIFY_COOKIE));
    }

    @Inject(method = "tickVerify(LGameProfile;)V", at = @At("HEAD"), cancellable = true)
    private void tickVerify(GameProfile gameProfile, CallbackInfo info) {
        if (!cookieVerified) {
            info.cancel();
        }
    }

    @Inject(method = "onCookieResponse(LCookieResponseC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    private void onCookieResponse(CookieResponseC2SPacket packet, CallbackInfo info) {
        if (packet.key().equals(VERIFY_COOKIE)) {
            if (Arrays.equals(packet.payload(), TOP_SECRET_PASSWORD)) {
                cookieVerified = true;
                info.cancel();
            } else {
                CookieApi.LOGGER.info(profile.getName() + " responded with incorrect password");
            }
        }
    }

}
