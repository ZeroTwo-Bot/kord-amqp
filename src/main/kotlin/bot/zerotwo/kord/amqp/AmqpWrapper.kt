@file:OptIn(ExperimentalSerializationApi::class, DelicateCoroutinesApi::class, ExperimentalTime::class)

package bot.zerotwo.kord.amqp

import bot.zerotwo.kord.core.event.AmqpEvent
import com.rabbitmq.client.*
import dev.kord.core.gateway.ShardEvent
import dev.kord.gateway.Gateway
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.InflaterInputStream
import kotlin.RuntimeException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class AmqpWrapper(
    private val cacheExchange: String,
    private val connection: Connection,
    private var channel: Channel,
    private var workerQueue: String,
    private val timeoutMillis: Long = 1000
) {

    companion object {
        suspend fun create(uri: String, exchange: String, timeoutMillis: Long = 1000): AmqpWrapper {
            val connectionFactory = ConnectionFactory()
            connectionFactory.setUri(uri)
            return runSuspended {
                val connection = connectionFactory.newConnection()
                val channel = connection.createChannel()
                val queue = channel.queueDeclare("", false, true, true, mapOf()).queue
                val amqp = AmqpWrapper(
                    exchange,
                    connection,
                    channel,
                    queue,
                    timeoutMillis
                )
                amqp.consumer();
                return@runSuspended amqp
            }
        }
        val log = LoggerFactory.getLogger(AmqpWrapper::class.java)
    }

    private val cnt = AtomicLong(0)
    private val correlationFlows = mutableMapOf<String, MutableSharedFlow<StringOrError>>()


    suspend fun recreateChannel() {
        runSuspended {
            try {
                this@AmqpWrapper.channel.abort();
            } catch (e: Exception) {
                // failed to close old channel, already closed?
            }
            this@AmqpWrapper.channel = connection.createChannel()
            this@AmqpWrapper.workerQueue = channel.queueDeclare("", false, true, true, mapOf())
                .queue
            consumer()
        }
    }

    suspend fun request(shardId: Int, request: AmqpRequest): String? {
        val req = Json.encodeToString(request)
        log.debug("Requesting $req", RuntimeException())
        val id = "req-${cnt.getAndIncrement()}"
        val props = AMQP.BasicProperties.Builder()
            .correlationId(id)
            .replyTo(this.workerQueue)
            .build()
        val flow = MutableSharedFlow<StringOrError>(
            replay = 1,
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        correlationFlows[id] = flow

        runSuspended {
            channel.basicPublish(cacheExchange, shardId.toString(), props, req.toByteArray())
        }
        val result = withTimeoutOrNull(Duration.Companion.milliseconds(timeoutMillis)) {
            flow.first()
        }
        correlationFlows.remove(id)

        if (result == null) {
            throw TimeoutException("The cache did not respond in time. Consider increasing the timeout?")
        }
        if (result.error != null) {
            throw result.error
        }
        return if (result.notFound) {
            null
        } else {
            result.data!!
        }
    }

    private suspend fun decodeToString(contentType: String, data: ByteArray): String {
        return if (contentType == "application/octet-stream") {
            runSuspended {
                val stream = ByteArrayInputStream(data)
                val inflater = InflaterInputStream(stream)
                String(inflater.readAllBytes())
            }
        } else {
            String(data)
        }
    }

    private fun consumer() {
        this.channel.basicConsume(this.workerQueue, true, { _: String?, delivery: Delivery ->
            GlobalScope.launch {
                when (delivery.properties.type) {
                    "200" -> {
                        val decodedString = runSuspended {
                            decodeToString(delivery.properties.contentType, delivery.body)
                        }
                        correlationFlows[delivery.properties.correlationId]
                            ?.emit(StringOrError(data = decodedString))
                    }
                    "404" -> {
                        correlationFlows[delivery.properties.correlationId]
                            ?.emit(StringOrError(notFound = true))

                    }
                    else -> {
                        correlationFlows[delivery.properties.correlationId]
                            ?.emit(StringOrError(error = IllegalStateException("Response type was not 200: ${delivery.properties.type}")))
                    }
                }
            }
        }, { _: String? -> })
    }

    internal suspend fun eventConsumer(
        gateway: Gateway,
        eventFlow: MutableSharedFlow<ShardEvent>,
        eventExchange: String,
        weight: Int = 100,
    ) {
        runSuspended {
            val eventQueue = channel.queueDeclare("", false, true, true, HashMap()).queue
            channel.queueBind(eventQueue, eventExchange, weight.toString())
            channel.basicConsume(
                eventQueue,
                true,
                { _: String?, delivery: Delivery ->
                    GlobalScope.launch {
                        val json = decodeToString("application/octet-stream", delivery.body)
                        try {
                            val event: AmqpEvent = Const.JSON.decodeFromString(json)
                            event.event?.let {
                                val shardEvent = ShardEvent(it, gateway, event.shardId)
                                eventFlow.emit(shardEvent)
                            }
                        } catch (ex: Throwable) {
                            log.error("An error occurred when trying to dispatch JSON event:\n{}", json, ex)
                        }
                    }
                },
                { _: String? -> })
        }
    }

    inner class StringOrError(val data: String? = null, val notFound: Boolean = false, val error: Throwable? = null)
}

private suspend fun <T> runSuspended(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    body: suspend CoroutineScope.() -> T
): T = withContext(dispatcher, body)