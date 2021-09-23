package bot.zerotwo.kord.core.event

import bot.zerotwo.kord.core.*
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.InteractionData
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.Event as CoreEvent
import dev.kord.gateway.Event as GatewayEvent
import dev.kord.core.event.interaction.*
import dev.kord.core.gateway.handler.GatewayEventInterceptor
import dev.kord.gateway.InteractionCreate
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

class Interceptor(
    private val kord: Kord,
    private val gateway: AmqpMasterGateway,
    private val events: MutableSharedFlow<CoreEvent>
) : GatewayEventInterceptor {

    override suspend fun start(): Job = gateway.events
        .buffer(Channel.UNLIMITED)
        .onEach { event ->
            val context = createContext(kord.coroutineContext, kord.selfId, event.shard, event.getGuildId())
            dispatch(context, event.event, event.shard)
        }
        .launchIn(kord)

    private suspend fun dispatch(context: CoroutineContext, event: GatewayEvent, shard: Int) {
        if (event is InteractionCreate) {
            handle(event, shard, context)
        }
    }

    private fun createContext(
        defaultContext: CoroutineContext,
        botId: Snowflake,
        shardId: Int,
        guildId: Snowflake?
    ): CoroutineContext {
        return defaultContext + RequestMeta(
            ContextKeys.REQUEST_META_KEY,
            guildId,
            shardId,
            botId,
        ) + EventCache(ContextKeys.EVENT_CACHE)
    }

    private suspend fun handle(event: InteractionCreate, shard: Int, context: CoroutineContext) {
        val data = InteractionData.from(event.interaction)
        val interaction = Interaction.from(data, kord)
        val coreEvent = when (interaction) {
            is GlobalChatInputCommandInteraction -> GlobalChatInputCommandInteractionCreateEvent(
                interaction,
                kord,
                shard,
                context
            )
            is GlobalUserCommandInteraction -> GlobalUserCommandInteractionCreateEvent(
                interaction,
                kord,
                shard,
                context
            )
            is GlobalMessageCommandInteraction -> GlobalMessageCommandInteractionCreateEvent(
                interaction,
                kord,
                shard,
                context
            )
            is GuildChatInputCommandInteraction -> GuildChatInputCommandInteractionCreateEvent(
                interaction,
                kord,
                shard,
                context
            )
            is GuildMessageCommandInteraction -> GuildMessageCommandInteractionCreateEvent(
                interaction,
                kord,
                shard,
                context
            )
            is GuildUserCommandInteraction -> GuildUserCommandInteractionCreateEvent(interaction, kord, shard, context)
            is ButtonInteraction -> ButtonInteractionCreateEvent(interaction, kord, shard, context)
            is SelectMenuInteraction -> SelectMenuInteractionCreateEvent(interaction, kord, shard, context)
            is UnknownComponentInteraction -> error("Unknown component.")
            is UnknownApplicationCommandInteraction -> error("Unknown component.")
        }
        events.emit(coreEvent)
    }
}