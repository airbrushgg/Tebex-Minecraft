package io.tebex.plugin

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import gg.airbrush.server.lib.OperatorSender
import gg.airbrush.server.plugins.Plugin
import gg.airbrush.server.server
import io.tebex.plugin.command.BuyCommand
import io.tebex.plugin.command.TebexCommand
import io.tebex.plugin.util.Multithreading
import io.tebex.sdk.SDK
import io.tebex.sdk.Tebex
import io.tebex.sdk.obj.Category
import io.tebex.sdk.obj.QueuedPlayer
import io.tebex.sdk.obj.ServerEvent
import io.tebex.sdk.obj.ServerEventType
import io.tebex.sdk.placeholder.PlaceholderManager
import io.tebex.sdk.platform.Platform
import io.tebex.sdk.platform.PlatformTelemetry
import io.tebex.sdk.platform.PlatformType
import io.tebex.sdk.platform.config.IPlatformConfig
import io.tebex.sdk.platform.config.ServerPlatformConfig
import io.tebex.sdk.request.response.ServerInformation
import io.tebex.sdk.util.CommandResult
import net.minestom.server.MinecraftServer
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerSpawnEvent
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

lateinit var platform: TebexPlugin

class TebexPlugin : Plugin(), Platform {
    val logger = Logger.getLogger("Tebex")
    val dataFolder = File("./plugins/tebex/")

    private lateinit var sdk: SDK
    private lateinit var config: ServerPlatformConfig
    private lateinit var placeholderManager: PlaceholderManager
    var storeInformation: ServerInformation? = null
    private val queuedPlayers = Maps.newConcurrentMap<Any, Int>()
    private var storeCategories: List<Category?>? = null
    private val serverEvents = mutableListOf<ServerEvent>()
    private var setup = false

    override fun setup() {
        platform = this

        try {
            val configYaml = initPlatformConfig()
            config = loadServerPlatformConfig(configYaml)
        } catch (e: IOException) {
            warning(
                "Failed to load configuration: " + e.message,
                "Check that your configuration is valid and in the proper format and reload the plugin. You may delete `Tebex/config.yml` and a new configuration will be generated."
            )
            return
        }

        Runtime.getRuntime().addShutdownHook(Thread { Multithreading.shutdown() })

        val commands = MinecraftServer.getCommandManager()
        val events = MinecraftServer.getGlobalEventHandler()

        Tebex.init(this)
        sdk = SDK(this, config.secretKey)

        placeholderManager = PlaceholderManager()
        placeholderManager.registerDefaults()

        init()

        commands.register(TebexCommand)

        if (config.isBuyCommandEnabled)
            commands.register(BuyCommand)

        events.addListener(PlayerSpawnEvent::class.java) { event ->
            onPlayerJoin(event.player)
        }

        executeAsync {
            if (config.secretKey.isNotEmpty()) {
                info("Loading store information...")
                getSDK().serverInformation
                    .thenAccept { information: ServerInformation ->
                        storeInformation = information
                    }
                    .exceptionally { error: Throwable ->
                        warning(
                            "Failed to load server information: " + error.message,
                            "Please check that your secret key is valid."
                        )
                        null
                    }
                getSDK().listing
                    .thenAccept { listing: List<Category?> ->
                        storeCategories = listing
                    }
                    .exceptionally { error: Throwable ->
                        warning(
                            "Failed to load store categories: " + error.message,
                            "Please check that your secret key is valid."
                        )
                        null
                    }
            }

            Multithreading.executeAsync({
                getSDK().serverInformation
                    .thenAccept { information: ServerInformation ->
                        storeInformation = information
                    }
                getSDK().listing
                    .thenAccept { listing: List<Category?> ->
                        storeCategories = listing
                    }
            }, 0, 30, TimeUnit.MINUTES)

            Multithreading.executeAsync({
                getSDK().sendPluginEvents()
            }, 0, 10, TimeUnit.MINUTES)

            Multithreading.executeAsync({
                val runEvents: List<ServerEvent> =
                    Lists.newArrayList(
                        serverEvents.subList(
                            0,
                            min(serverEvents.size.toDouble(), 750.0).toInt()
                        )
                    )
                if (runEvents.isEmpty()) return@executeAsync
                sdk.sendJoinEvents(runEvents)
                    .thenAccept { aVoid: Boolean? ->
                        serverEvents.removeAll(runEvents)
                        debug("Successfully sent join events")
                    }
                    .exceptionally { throwable: Throwable? ->
                        error("Failed to send join events", throwable)
                        null
                    }
            }, 0, 1, TimeUnit.MINUTES)
        }

        logger.info("Tebex is ready.")
    }

    override fun teardown() {}

    private fun onPlayerJoin(player: Player) {
        val playerId: Any = getPlayerId(player.username, player.uuid)
        serverEvents += ServerEvent(
                player.uuid.toString(),
                player.username,
                player.playerConnection.remoteAddress.toString(),
                ServerEventType.JOIN,
                Date().toString()
            )

        if (playerId !in queuedPlayers)
            return

        handleOnlineCommands(
            QueuedPlayer(
                queuedPlayers[playerId]!!,
                player.username,
                player.uuid.toString()
            )
        )
    }

    override fun getType(): PlatformType {
        return PlatformType.BUKKIT
    }

    override fun getStoreType(): String {
        return storeInformation?.store?.gameType ?: ""
    }

    override fun getSDK(): SDK {
        return sdk
    }

    override fun getDirectory(): File {
        return dataFolder
    }

    override fun getIsSetup(): Boolean {
        return setup
    }

    override fun setIsSetup(setup: Boolean) {
        this.setup = setup;
    }

    override fun isOnlineMode(): Boolean {
        return true
    }

    override fun configure() {
        isSetup = true
        performCheck()
        // sdk.sendTelemetry()
    }

    override fun halt() {
        isSetup = false
    }

    override fun getPlaceholderManager(): PlaceholderManager {
        return placeholderManager
    }

    override fun getQueuedPlayers(): MutableMap<Any, Int> {
        return queuedPlayers
    }

    override fun dispatchCommand(command: String): CommandResult {
        val result = MinecraftServer.getCommandManager().execute(
            OperatorSender(),
            command
        )

        return CommandResult.from(result.type == net.minestom.server.command.builder.CommandResult.Type.SUCCESS)
    }

    override fun executeAsync(runnable: Runnable?) {
        Multithreading.executeAsync(runnable)
    }

    override fun executeAsyncLater(runnable: Runnable?, time: Long, unit: TimeUnit?) {
        Multithreading.executeAsyncLater(runnable, time, unit)
    }

    override fun executeBlocking(runnable: Runnable?) {
        Multithreading.executeBlocking(runnable)
    }

    override fun executeBlockingLater(runnable: Runnable?, time: Long, unit: TimeUnit?) {
        Multithreading.executeBlockingLater(runnable, time, unit)
    }

    override fun isPlayerOnline(player: Any?): Boolean {
        return getPlayer(player).isPresent
    }

    private fun getPlayer(player: Any?): Optional<Player> {
        val connectionManager = MinecraftServer.getConnectionManager()

        if (isOnlineMode && player is UUID) {
            val fetchedPlayer = connectionManager.getOnlinePlayerByUuid(player)
            return Optional.ofNullable(fetchedPlayer)
        }

        return Optional.ofNullable(connectionManager.getOnlinePlayerByUsername(player as String))
    }

    override fun getFreeSlots(player: Any?): Int {
        val fetchedPlayer = getPlayer(player).getOrNull()
            ?: return -1

        return fetchedPlayer.inventory.itemStacks
            .filter { it.isAir }
            .size
    }

    override fun getVersion(): String {
        return "2.0.6"
    }

    override fun log(level: Level?, message: String?) {
        logger.log(level, message)
    }

    override fun setStoreInfo(info: ServerInformation?) {
        this.storeInformation = info
    }

    fun getStoreCategories() = storeCategories

    override fun setStoreCategories(categories: MutableList<Category>) {
        this.storeCategories = categories
    }

    override fun getPlatformConfig(): IPlatformConfig {
        return config
    }

    override fun getTelemetry(): PlatformTelemetry {
        var serverVersion = "0.3.2"

        val pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)")
        val matcher = pattern.matcher(serverVersion)
        if (matcher.find()) {
            serverVersion = matcher.group(1)
        }

        return PlatformTelemetry(
            version,
            "Airbrush / Minestom",
            serverVersion,
            System.getProperty("java.version"),
            System.getProperty("os.arch"),
            isOnlineMode
        )
    }

    override fun getServerIp(): String {
        return MinecraftServer.getServer().address
    }
}