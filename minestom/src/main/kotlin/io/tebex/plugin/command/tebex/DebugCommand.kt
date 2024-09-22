package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import io.tebex.sdk.platform.config.ServerPlatformConfig
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType

object DebugCommand : SubCommand(
    "debug",
    "Enables more verbose logging.",
    "tebex.debug"
) {
    private val shouldEnable = ArgumentType.Boolean("enable")

    init {
        addSyntax(shouldEnable)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val config = platform.platformConfig as ServerPlatformConfig
        val configFile = config.yamlDocument

        val enable = context.get(shouldEnable)
        config.isVerbose = enable
        configFile.set("verbose", enable)
        sender.message("Debug mode ${if (enable) "enabled" else "disabled"}")

        try {
            configFile.save()
        } catch (exception: Exception) {
            sender.message("Failed to save configuration file.")
        }
    }
}