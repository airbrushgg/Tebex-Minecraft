package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.checkStoreIsSetup
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType

object BanCommand : SubCommand(
    "ban",
    "Bans a player from using the webstore. Unbans can only be made via the web panel.",
    "tebex.ban"
) {
    private val player = ArgumentType.String("player uuid")
    private val reason = ArgumentType.String("reason").setDefaultValue("")
    private val ip = ArgumentType.String("ip").setDefaultValue("")

    init {
        addSyntax(player, reason, ip)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = context.get(player)
        val reason = context.get(reason)
        val ip = context.get(ip)

        if (sender.checkStoreIsSetup())
            return

        try {
            val success = platform.sdk.createBan(player, ip, reason).get()

            if (success)
                sender.message("Player banned successfully.")
            else sender.message("Failed to ban player.")
        } catch (exception: Exception) {
            sender.message("Error while banning player: ${exception.message}")
        }
    }
}