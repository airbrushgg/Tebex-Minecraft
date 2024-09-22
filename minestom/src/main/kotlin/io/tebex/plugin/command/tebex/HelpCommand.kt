package io.tebex.plugin.command.tebex

import gg.airbrush.server.lib.mm
import io.tebex.plugin.command.TebexCommand
import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.ArgumentType
import java.util.function.Function

object HelpCommand : SubCommand(
    "help",
    "Shows this help page.",
    "tebex.help"
) {
    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.message("Plugin commands:")

        for (command in TebexCommand.subcommands.sortedBy { it.name }) {
            for (syntax in command.syntaxes) {
                val usage = StringBuilder(" <dark_gray>-</dark_gray> <white>/tebex ${command.name}")

                for (argument in syntax.arguments) {
                    if (argument is ArgumentLiteral) usage.append(" ${argument.id}")
                    else usage.append(" <${argument.id}>")
                }

                if (command is SubCommand)
                    usage.append(" <i>(${command.description})")

                sender.sendMessage(usage.toString().mm())
            }
        }
    }
}