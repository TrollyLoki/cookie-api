package net.trollyloki.cookieapi;

import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public interface CookieNetworkHandler {

    /**
     * Stores a cookie on the player's client. A single cookie cannot be longer than 5,120 bytes.
     *
     * @param identifier cookie identifier
     * @param payload cookie data
     * @throws IllegalArgumentException if payload is too long
     */
    void storeCookie(Identifier identifier, byte[] payload);

    /**
     * Requests a cookie from the player's client.
     *
     * @param identifier cookie identifier
     * @return future to be completed with the (possibly null) cookie data
     */
    CompletableFuture<byte[]> requestCookie(Identifier identifier);

}
