package io.tebex.plugin.command.tebex

import gg.airbrush.server.server
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import io.tebex.sdk.exception.ServerNotFoundException
import io.tebex.sdk.platform.config.ServerPlatformConfig
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType

object SecretCommand : SubCommand(
    "secret",
    "Connects to your Tebex store.",
    "tebex.secret"
) {
    private val key = ArgumentType.String("key")

    init {
        addSyntax(key)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val key = context.get(key)
        val sdk = platform.sdk
        val config = platform.platformConfig as ServerPlatformConfig
        val configFile = config.yamlDocument

        sdk.secretKey = key

        sdk.serverInformation
            .thenAccept { serverInformation ->
                config.secretKey = key
                configFile.set("server.secret-key", key)

                try {
                    configFile.save()
                } catch (exception: Exception) {
                    sender.message("Failed to send message: ${exception.message}")
                }

                sender.message("Connected to <aqua>${serverInformation.server.name}</aqua>.")
                platform.configure()
            }
            .exceptionally { exception ->
                val cause = exception.cause
                    ?: return@exceptionally null

                if (cause is ServerNotFoundException) {
                    sender.message("Server not found. Please check your secret key.")
                    platform.halt()
                } else {
                    sender.message("An error occurred: ${cause.message}")
                    cause.printStackTrace()
                }

                null
            }
    }
}