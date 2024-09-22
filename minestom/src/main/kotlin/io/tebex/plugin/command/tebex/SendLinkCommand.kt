package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType

object SendLinkCommand : SubCommand(
    "sendlink",
    "Creates payment link for a package and sends it to a player",
    "tebex.sendlink"
) {
    private val username = ArgumentType.String("username")
    private val packageID = ArgumentType.Integer("package")

    init {
        addSyntax(username, packageID)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val username = context.get(username).trim()
        val packageID = context.get(packageID)

        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(username)
            ?: return sender.message("Could not find a player with that name on the server.")

        try {
            val checkoutURL = platform.sdk.createCheckoutUrl(packageID, player.username).get()
            player.message("A checkout link has been created for you. Please complete payment: ${checkoutURL.url}")
        } catch (exception: Exception) {
            sender.message("Failed to get checkout link for package: ${exception.message}")
        }
    }
}