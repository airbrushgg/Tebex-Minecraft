package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.checkStoreIsSetup
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import io.tebex.sdk.obj.CheckoutUrl
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object CheckoutCommand : SubCommand(
    "checkout",
    "Creates payment link for a package",
    "tebex.checkout"
) {
    private val packageID = ArgumentType.Integer("package")

    init {
        addSyntax(packageID)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender.checkStoreIsSetup())
            return

        val packageID = context.get(packageID)

        try {
            val name = if (sender is Player) sender.username else "Console"
            val checkoutURL = platform.sdk.createCheckoutUrl(packageID, name).get()
            sender.message("Checkout started! Click here to complete payment: ${checkoutURL.url}")
        } catch (exception: Exception) {
            sender.message("Failed to get checkout link for package: ${exception.message}")
        }
    }
}