package io.tebex.plugin.gui

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.item.ItemStack

private val handlers = mutableMapOf<Inventory, MutableMap<Int, ClickHandler>>()
typealias ClickHandler = ClickHandlerContext.(InventoryClickEvent) -> Unit
typealias CloseHandler = (InventoryCloseEvent) -> Unit

class ClickHandlerContext(
    private val clickEvent: EventListener<InventoryClickEvent>,
    private val closeEvent: EventNode<Event>
) {
    fun resolve() {
        val events = MinecraftServer.getGlobalEventHandler()
        events.removeChild(closeEvent)
    }
}

fun createInventory(rows: Int, title: Component, closeHandler: CloseHandler = {}): Inventory {
    val events = MinecraftServer.getGlobalEventHandler()
    val inventory = Inventory(getRowType(rows), title)
    handlers[inventory] = mutableMapOf()

    lateinit var clickEvent: EventListener<InventoryClickEvent>
    lateinit var closeEvent: EventNode<Event>

    inventory.addInventoryCondition { player, slot, clickType, result ->
        val handler = handlers[inventory]!![slot]
            ?: return@addInventoryCondition

        result.isCancel = true
        handler.invoke(ClickHandlerContext(clickEvent, closeEvent), InventoryClickEvent(
            inventory,
            player,
            slot,
            clickType,
            result.clickedItem,
            result.cursorItem
        ))
    }

    clickEvent = EventListener.of(InventoryClickEvent::class.java) { event ->
        if (event.inventory != inventory)
            return@of

        val handler = handlers[inventory]!![event.slot]
            ?: return@of

        val context = ClickHandlerContext(clickEvent, closeEvent)
        handler.invoke(context, event)
    }

    closeEvent = events.addListener(InventoryCloseEvent::class.java) { event ->
        if (event.inventory != inventory)
            return@addListener

        closeHandler.invoke(event)
        events.removeChild(closeEvent)
    }

    return inventory
}

fun setItem(inventory: Inventory, slot: Int, itemStack: ItemStack, handler: ClickHandler = {}) {
    inventory.setItemStack(slot, itemStack)
    handlers[inventory]!![slot] = handler
}

fun addItem(inventory: Inventory, itemStack: ItemStack, handler: ClickHandler = {}) {
    val slot = inventory.itemStacks.indexOfFirst { it.isAir }
    setItem(inventory, slot, itemStack, handler)
}