package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.checkStoreIsSetup
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object InfoCommand : SubCommand(
    "info",
    "Gets information about this server's connected store.",
    "tebex.info"
) {
    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender.checkStoreIsSetup())
            return

        val storeInformation = platform.storeInformation!!
        val store = storeInformation.store
        val server = storeInformation.server

        sender.message("Information for this server:")
        sender.message("${server.name} for webstore ${store.name}")
        sender.message("Server prices are in ${store.currency.iso4217}")
        sender.message("Webstore domain ${store.domain}")
    }
}