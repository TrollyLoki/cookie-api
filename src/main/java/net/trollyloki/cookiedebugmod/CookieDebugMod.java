package net.trollyloki.cookiedebugmod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CookieDebugMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("cookie-debug-mod");

	private record CookieRequest(UUID uuid, Identifier identifier) {
		private CookieRequest(ServerPlayerEntity player, Identifier identifier) {
			this(player.getUuid(), identifier);
		}
	}

	private static final Map<CookieRequest, Queue<ServerCommandSource>> COOKIE_REQUESTS = new HashMap<>();

	private void requestCookie(ServerPlayerEntity player, Identifier identifier, ServerCommandSource source) {
		CookieRequest cookieRequest = new CookieRequest(player, identifier);
		COOKIE_REQUESTS.computeIfAbsent(cookieRequest, k -> new LinkedList<>()).add(source);
		player.networkHandler.sendPacket(new CookieRequestS2CPacket(identifier));
	}

	public static void onCookieResponse(ServerPlayerEntity player, Identifier identifier, byte[] payload) {
		CookieRequest cookieRequest = new CookieRequest(player, identifier);
		if (!COOKIE_REQUESTS.containsKey(cookieRequest)) {
			LOGGER.warn("Unexpected cookie response packet received from " + player.getGameProfile().getName());
			return;
		}

		Queue<ServerCommandSource> queue = COOKIE_REQUESTS.get(cookieRequest);
		queue.remove().sendFeedback(() -> {

			MutableText text = Text.literal("Cookie ")
					.append(Text.of(identifier))
					.append(Text.literal(" from "))
					.append(player.getStyledDisplayName())
					.append(Text.literal(": "));
			if (payload == null) {
				text.append(Text.literal("null"));
			} else {
				String string = new String(payload, StandardCharsets.UTF_8);
				text.append(Text.literal(string).styled(style -> style
						.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")))
				));
			}
			return text;

		}, false);
		if (queue.isEmpty()) {
			COOKIE_REQUESTS.remove(cookieRequest);
		}
	}

	public static final SimpleCommandExceptionType PAYLOAD_TOO_LONG = new SimpleCommandExceptionType(Text.literal("Payload too long"));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (!environment.dedicated)
                return;
			dispatcher.register(literal("cookie").requires(source -> source.hasPermissionLevel(3))
					.then(argument("player", player())
							.then(argument("cookie", identifier())
									.executes(context -> {
										ServerPlayerEntity player = getPlayer(context, "player");
										Identifier identifier = getIdentifier(context, "cookie");
										requestCookie(player, identifier, context.getSource());
										return Command.SINGLE_SUCCESS;
									})
									.then(argument("payload", greedyString())
											.executes(context -> {
												ServerPlayerEntity player = getPlayer(context, "player");
												Identifier identifier = getIdentifier(context, "cookie");
												String string = getString(context, "payload");
												byte[] payload = string.getBytes(StandardCharsets.UTF_8);
												if (payload.length > StoreCookieS2CPacket.MAX_COOKIE_LENGTH) {
													throw PAYLOAD_TOO_LONG.create();
												}

												player.networkHandler.sendPacket(new StoreCookieS2CPacket(identifier, payload));
												context.getSource().sendFeedback(() -> Text.literal("Cookie ")
														.append(Text.of(identifier))
														.append(Text.literal(" stored on "))
														.append(player.getStyledDisplayName()), true);
												return Command.SINGLE_SUCCESS;
											})
									)
							)
					)
			);
		});
	}
}