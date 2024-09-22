package io.tebex.plugin.lib

import gg.airbrush.server.lib.mm
import io.tebex.plugin.platform
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.command.CommandSender

fun CommandSender.message(message: String) {
    sendMessage("<aqua>[Tebex] <gray>$message".mm())
}

fun CommandSender.checkStoreIsSetup(): Boolean {
    if (!platform.getIsSetup()) {
        message("This server is not connected to a webstore. Use /tebex secret to set your store key.")
        return true
    }

    return false
}

fun String.component(): Component {
    return Component.text(this)
}

fun Component.plainText(): Component {
    return PlainTextComponentSerializer.plainText().serialize(this).component()
}