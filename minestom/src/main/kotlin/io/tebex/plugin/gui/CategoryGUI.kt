package io.tebex.plugin.gui

import gg.airbrush.server.lib.mm
import io.tebex.plugin.platform
import io.tebex.sdk.obj.*
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

private val config = platform.platformConfig.yamlDocument

fun openCategoryGUI(player: Player, category: ICategory) {
    val packages = category.packages.sortedBy(CategoryPackage::getOrder)
    val title = config.getString("gui.menu.category.title")
        .replace("%category%", category.name).mm()
    val rows = config.getInt("gui.menu.home.rows").let {
        if (it < 1) category.packages.size / 9 + 1
        else it
    }

    val events = MinecraftServer.getGlobalEventHandler()
    val inventory = createInventory(rows, title)

    val backButton = createItem(null, "gui.item.back", "<white>Back")

    when (category) {
        is Category -> {
            for (subCategory in category.subCategories) {
                val item = createItem(subCategory, "gui.item.category", subCategory.name)
                addItem(inventory, item) { event ->
                    if (event.player != player || event.clickedItem != item)
                        return@addItem

                    openCategoryGUI(player, subCategory)
                    resolve()
                }
            }

            setItem(inventory, rows * 9 - 5, backButton) { event ->
                if (event.player != player)
                    return@setItem

                openBuyGUI(player)
                resolve()
            }
        }
        is SubCategory -> {
            inventory.title = config.getString("gui.menu.sub-category.title")
                .replace("%category%", category.parent.name)
                .replace("%sub_category%", category.name)
                .mm()

            setItem(inventory, rows * 9 - 5, backButton) { event ->
                if (event.player != player)
                    return@setItem

                openCategoryGUI(player, category.parent)
                resolve()
            }
        }
    }

    for (pkg in packages) {
        val section = if (pkg.hasSale()) "gui.item.package-sale" else "gui.item.package"
        val item = createItem(pkg, section, pkg.name)
        addItem(inventory, item) { event ->
            if (event.player != player || event.clickedItem != item)
                return@addItem

            player.closeInventory()

            // Create Checkout Url
            platform.sdk.createCheckoutUrl(pkg.id, player.username)
                .thenAccept { checkout: CheckoutUrl ->
                    player.sendMessage("<green>You can checkout here:".mm())
                    player.sendMessage("<green><click:open_url:${checkout.url}>${checkout.url}".mm())
                }
                .exceptionally { ex: Throwable? ->
                    player.sendMessage("<red>Failed to create checkout URL. Please contact an administrator.".mm())
                    platform.error("Failed to create checkout URL for a user.", ex)
                    null
                }
        }
    }

    player.openInventory(inventory)
}