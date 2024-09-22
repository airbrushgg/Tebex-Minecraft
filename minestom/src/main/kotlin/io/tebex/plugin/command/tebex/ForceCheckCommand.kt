package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.checkStoreIsSetup
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object ForceCheckCommand : SubCommand(
    "forcecheck",
    "Checks immediately for new purchases.",
    "tebex.forcecheck"
) {
    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender.checkStoreIsSetup())
            return

        if (sender !is ConsoleSender)
            sender.message("Performing force check...")

        platform.performCheck(false)
    }
}