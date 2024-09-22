package io.tebex.plugin.command.tebex

import io.tebex.plugin.lib.message
import io.tebex.plugin.platform
import io.tebex.sdk.obj.CommunityGoal
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object GoalsCommand : SubCommand(
    "goals",
    "Shows active and completed community goals.",
    "tebex.goals"
) {
    override fun apply(sender: CommandSender, context: CommandContext) {
        try {
            val goals = platform.sdk.communityGoals.get()

            sender.message("Community Goals:")

            for (goal in goals)
                if (goal.status != CommunityGoal.Status.DISABLED)
                    sender.message(String.format("- ${goal.name} (%.2f/%.2f) [${goal.status}]", goal.current, goal.target))
        } catch (exception: Exception) {
            sender.message("Unexpected response: ${exception.message}")
        }
    }
}