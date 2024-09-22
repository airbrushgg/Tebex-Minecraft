package io.tebex.plugin.gui

import gg.airbrush.server.lib.mm
import io.tebex.plugin.lib.plainText
import io.tebex.plugin.platform
import io.tebex.sdk.obj.Category
import io.tebex.sdk.obj.CategoryPackage
import io.tebex.sdk.obj.ICategory
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.EnchantmentList
import net.minestom.server.item.component.Unbreakable
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

private val config = platform.platformConfig.yamlDocument

fun openBuyGUI(player: Player) {
    if (player.openInventory != null) {
        player.sendMessage("<error>You already have an inventory open.".mm())
        return
    }

    val categories = platform.getStoreCategories()?.filterNotNull()?.sortedBy(Category::getOrder)

    if (categories == null) {
        player.sendMessage("Failed to get listing. Please contact an administrator.".mm())
        platform.warning(
            "Player " + player.name + " used buy command, but no listings are active in your store.",
            "Ensure your store is set up and has at least one active listing. Use /tebex reload to load new listings."
        )
        return
    }

    val title = config.getString("gui.menu.home.title", "Server Shop").mm()
    val rows = config.getInt("gui.menu.home.rows").let {
        if (it < 1) categories.size / 9 + 1
        else it
    }

    val events = MinecraftServer.getGlobalEventHandler()
    val inventory = Inventory(getRowType(rows), title)

    for (category in categories)
        inventory.addItemStack(createItem(category, "gui.item.category", category.name))

    lateinit var clickEvent: EventListener<InventoryClickEvent>
    lateinit var closeEvent: EventNode<Event>

    inventory.addInventoryCondition { _, slot, clickType, result ->
        result.isCancel = true
        clickEvent.run(InventoryClickEvent(
            inventory,
            player,
            slot,
            clickType,
            result.clickedItem,
            result.cursorItem
        ))
    }

    clickEvent = EventListener.of(InventoryClickEvent::class.java) { event ->
        if (event.player != player)
            return@of

        val slot = event.slot
        val category = categories.getOrNull(slot)
            ?: return@of

        openCategoryGUI(player, category)
        events.removeChild(closeEvent)
    }

    closeEvent = events.addListener(InventoryCloseEvent::class.java) { event ->
        if (event.player != player)
            return@addListener

        events.removeChild(closeEvent)
    }

    platform.executeBlocking { player.openInventory(inventory) }
}

fun createItem(obj: Any?, section: String, defaultName: String): ItemStack {
    val section = config.getSection(section)
    val materialType = section.getString("material")
    val lore = section.getStringList("lore")
    val name = section.getString("name", defaultName)

    val material = Material.fromNamespaceId(materialType) ?: Material.BOOK
    val styledName = handlePlaceholders(obj, name).mm()
        .decoration(TextDecoration.ITALIC, false)
    val styledLore = lore.map { handlePlaceholders(obj, it).mm()
        .decoration(TextDecoration.ITALIC, false) }

    val builder = ItemStack.builder(material)
        .customName(styledName)
        .lore(styledLore)
        .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP)
        .set(ItemComponent.UNBREAKABLE, Unbreakable.DEFAULT)

    if (obj is CategoryPackage && obj.hasSale())
        builder.set(ItemComponent.ENCHANTMENT_GLINT_OVERRIDE, true)

    return builder.build()
}

fun handlePlaceholders(obj: Any?, text: String): String {
    val format = DecimalFormat("#.##")

    return when (obj) {
        is ICategory -> text.replace("%category%", obj.name)
        is CategoryPackage -> text.replace("%package_name%", obj.name)
            .replace("%package_price%", format.format(obj.price))
            .replace("%package_currency_name%", platform.storeInformation!!.store.currency.iso4217)
            .replace("%package_currency%", platform.storeInformation!!.store.currency.symbol)
            .let {
                if (obj.hasSale())
                    it.replace("%package_discount%", format.format(obj.sale.discount))
                        .replace("%package_sale_price%", format.format(obj.price - obj.sale.discount))
                else it
            }
        else -> text
    }
}

fun getRowType(rows: Int): InventoryType {
    val safeRows = max(min(rows, 6), 1)
    return InventoryType.valueOf("CHEST_${safeRows}_ROW")
}