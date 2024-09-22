package io.tebex.plugin.command

import gg.airbrush.server.lib.mm
import io.tebex.plugin.command.tebex.*
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object TebexCommand : Command("tebex", "buycraft"), CommandExecutor {
    init {
        defaultExecutor = this

        addSubcommand(BanCommand)
        addSubcommand(CheckoutCommand)
        addSubcommand(DebugCommand)
        addSubcommand(ForceCheckCommand)
        addSubcommand(GoalsCommand)
        addSubcommand(HelpCommand)
        addSubcommand(InfoCommand)
        addSubcommand(LookupCommand)
        addSubcommand(ReloadCommand)
        addSubcommand(ReportCommand)
        addSubcommand(SecretCommand)
        addSubcommand(SendLinkCommand)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("<dark_gray>[Tebex] <gray>Welcome to Tebex!".mm())
        sender.sendMessage("<dark_gray>[Tebex] <gray>This server is running version <white>${platform.version}</white>.".mm())
    }
}