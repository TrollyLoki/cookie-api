package net.trollyloki.cookiedebugmod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
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
										player.networkHandler.sendPacket(new CookieRequestS2CPacket(identifier));
										context.getSource().sendFeedback(() -> Text.literal(
												"Cookie request packet with identifier: \"" + identifier + "\" sent to " + player.getName()
										), false);
										return Command.SINGLE_SUCCESS;
									})
									.then(argument("payload", string())
											.executes(context -> {
												ServerPlayerEntity player = getPlayer(context, "player");
												Identifier identifier = getIdentifier(context, "cookie");
												String string = getString(context, "payload");
												byte[] payload = string.getBytes(StandardCharsets.UTF_8);
												if (payload.length > StoreCookieS2CPacket.MAX_COOKIE_LENGTH) {
													throw PAYLOAD_TOO_LONG.create();
												}

												player.networkHandler.sendPacket(new StoreCookieS2CPacket(identifier, payload));
												context.getSource().sendFeedback(() -> Text.literal("Cookie " + identifier + " stored on ")
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