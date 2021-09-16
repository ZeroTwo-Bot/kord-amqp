package bot.zerotwo.kord.core

import bot.zerotwo.kord.amqp.AmqpWrapper
import bot.zerotwo.kord.cache.AmqpCacheStrategy
import bot.zerotwo.kord.core.event.toGuildId
import dev.kord.cache.api.DataCache
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.builder.kord.Shards
import dev.kord.core.event.Event
import dev.kord.core.gateway.ShardEvent
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.rest.ratelimit.ExclusionRequestRateLimiter
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.RequestHandler
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class, KordExperimental::class)
suspend inline fun AmqpKord(
    token: String,
    totalShards: Int,
    amqpUri: String,
    events: List<String>,
    builder: AmqpKordBuilder.() -> Unit = {}
): Kord {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    return AmqpKordBuilder(token, totalShards, amqpUri, events).apply(builder).build()
}

class AmqpKordBuilder(val token: String, val totalShards: Int, val amqpUri: String, val events: List<String>) {

    var cacheExchange: String = "cache"

    var eventExchange: String = "events"

    var eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    var shardEventFlow: MutableSharedFlow<ShardEvent> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    var httpClient: HttpClient? = null

    var handlerBuilder: (resources: ClientResources) -> RequestHandler =
        { KtorRequestHandler(it.httpClient, ExclusionRequestRateLimiter()) }

    var defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    var defaultStrategyBuilder: (amqp: AmqpWrapper) -> EntitySupplyStrategy<*> = { amqp ->
        AmqpCacheStrategy(amqp, null, totalShards)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun build(): Kord {
        val amqp = AmqpWrapper.create(amqpUri, cacheExchange)
        val amqpGateway = AmqpGateway(amqp)
        amqp.eventConsumer(amqpGateway, shardEventFlow, eventExchange, events)

        val selfId = getBotIdFromToken(token)

        val resources = ClientResources(
            token,
            selfId,
            Shards(0),
            httpClient.configure(token),
            defaultStrategyBuilder.invoke(amqp),
            Intents.none
        )

        val rest = RestClient(handlerBuilder(resources))

        val masterGateway = AmqpMasterGateway(shardEventFlow, mapOf(0 to amqpGateway))
        return Kord(
            resources,
            DataCache.none(),
            masterGateway,
            rest,
            selfId,
            eventFlow,
            defaultDispatcher,
        ) {
            RequestMeta(
                ContextKeys.REQUEST_META_KEY,
                this.toGuildId(),
                this.shard,
                null
            ) + EventCache(ContextKeys.EVENT_CACHE)
        }
    }
}

internal fun getBotIdFromToken(token: String): Snowflake {
    try {
        val bytes = Base64.getDecoder().decode(token.split(""".""").first())
        return Snowflake(String(bytes))
    } catch (exception: IllegalArgumentException) {
        throw IllegalArgumentException("Malformed bot token: '$token'. Make sure that your token is correct.")
    }
}


internal fun HttpClientConfig<*>.defaultConfig(token: String) {
    expectSuccess = false
    defaultRequest {
        header("Authorization", "Bot $token")
    }

    install(JsonFeature)
}

internal fun HttpClient?.configure(token: String): HttpClient {
    if (this != null) return this.config {
        defaultConfig(token)
    }

    val json = Json {
        encodeDefaults = false
        allowStructuredMapKeys = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    return HttpClient(CIO) {
        defaultConfig(token)
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }
}