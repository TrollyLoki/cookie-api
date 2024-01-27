package net.trollyloki.cookieapi;

import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public interface CookieNetworkHandler {

    /**
     * Requests a cookie from the player's client.
     *
     * @param identifier cookie identifier
     * @return future to be completed with the (possibly null) cookie data
     */
    CompletableFuture<byte[]> requestCookie(Identifier identifier);

}
