package io.tebex.plugin.command.tebex

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument

abstract class SubCommand(
    name: String,
    val description: String,
    val permission: String,
    vararg aliases: String
) : Command(name, *aliases), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { it.hasPermission(permission) }
    }

    fun addSyntax(vararg args: Argument<*>) {
        addSyntax(this, *args)
    }

    fun setCondition(condition: (CommandSender) -> Boolean) {
        setCondition { sender, _ -> condition.invoke(sender) }
    }
}