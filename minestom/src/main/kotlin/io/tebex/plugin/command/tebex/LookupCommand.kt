package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.checkStoreIsSetup
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import io.tebex.sdk.obj.PlayerLookupInfo
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType

object LookupCommand : SubCommand(
    "lookup",
    "Gets user transaction info from your webstore.",
    "tebex.lookup"
) {
    private val username = ArgumentType.String("username")

    init {
        addSyntax(username)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender.checkStoreIsSetup())
            return

        val username = context.get(username)
        lateinit var lookup: PlayerLookupInfo

        try {
            lookup = platform.sdk.getPlayerLookupInfo(username).get()
        } catch (exception: Exception) {
            sender.message("Failed to complete player lookup: ${exception.message}")
            return
        }

        val player = lookup.lookupPlayer
        sender.message("Username: ${player.username}")
        sender.message("Id: ${player.id}")
        sender.message("Chargeback Rate: ${lookup.chargebackRate}")
        sender.message("Bans Total: ${lookup.banCount}")
        sender.message("Payments: ${lookup.payments.size}")
    }
}