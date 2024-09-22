package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType

object ReportCommand : SubCommand(
    "report",
    "Reports a problem to Tebex along with information about your webstore, server, etc.",
    "tebex.report"
) {
    private val message = ArgumentType.String("message")

    init {
        addSyntax(message)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val message = context.get(message)

        if (message.isBlank())
            return sender.message("A message is required for your report.")

        sender.message("Sending your report to Tebex...")
        platform.error("User reported error in-game: $message")
        sender.message("Report sent successfully.")
    }
}