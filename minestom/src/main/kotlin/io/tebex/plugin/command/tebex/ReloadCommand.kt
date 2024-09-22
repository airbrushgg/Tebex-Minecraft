package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

object ReloadCommand : SubCommand(
    "reload",
    "Reloads the plugin.",
    "tebex.reload"
) {
    override fun apply(sender: CommandSender, context: CommandContext) {
        try {
            val configYaml = platform.initPlatformConfig()
            platform.loadServerPlatformConfig(configYaml)
            platform.refreshListings()
            platform.sdk.sendPluginEvents() // is this telemetry?

            sender.message("Successfully reloaded.")
        } catch (exception: Exception) {
            sender.message("Failed to reload the plugin: ${exception.message}")
        }
    }
}