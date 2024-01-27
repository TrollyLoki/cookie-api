package net.trollyloki.cookieapi;

import net.minecraft.util.Identifier;

public interface CookiePlayNetworkHandler extends CookieNetworkHandler {

    /**
     * Stores a cookie on the player's client. A single cookie cannot be longer than 5,120 bytes.
     *
     * @param identifier cookie identifier
     * @param payload cookie data
     * @throws IllegalArgumentException if payload is too long
     */
    void storeCookie(Identifier identifier, byte[] payload);

}
