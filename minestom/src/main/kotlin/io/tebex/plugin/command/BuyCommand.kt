package io.tebex.plugin.command

import io.tebex.plugin.gui.openBuyGUI
import io.tebex.plugin.lib.message
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player

object BuyCommand : Command("buy"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player)
            return sender.message("You must be a player to run this command!")

        openBuyGUI(sender)
    }
}