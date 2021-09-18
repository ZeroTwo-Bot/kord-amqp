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
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.InflaterInputStream
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class AmqpWrapper(
    private val cacheExchange: String,
    private val connection: Connection,
    private var channel: Channel,
    private var workerQueue: String
) {

    companion object {
        suspend fun create(uri: String, exchange: String): AmqpWrapper {
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
                    queue
                )
                amqp.consumer();
                return@runSuspended amqp
            }
        }
    }

    private val cnt = AtomicLong(0)
    private val correlationFlows = mutableMapOf<String, MutableSharedFlow<String>>()


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
        val id = "req-${cnt.getAndIncrement()}"
        val props = AMQP.BasicProperties.Builder()
            .correlationId(id)
            .replyTo(this.workerQueue)
            .build()
        val req = Json.encodeToString(request)
        val flow = MutableSharedFlow<String>(
            replay = 1,
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        correlationFlows[id] = flow

        runSuspended {
            channel.basicPublish(cacheExchange, shardId.toString(), props, req.toByteArray())
        }
        val result = withTimeoutOrNull(Duration.Companion.milliseconds(500)) {
            flow.first()
        }
        correlationFlows.remove(id)
        return result
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
            if (delivery.properties.type != "200") {
                GlobalScope.launch {
                    runSuspended {
                        correlationFlows[delivery.properties.correlationId]
                            ?.emit(decodeToString(delivery.properties.contentType, delivery.body))
                    }
                }
            } else {
                throw IllegalStateException("Response type was not 200: ${delivery.properties.type}")
            }
        }, { _: String? -> })
    }

    internal suspend fun eventConsumer(
        gateway: Gateway,
        events: MutableSharedFlow<ShardEvent>,
        eventQueue: String,
    ) {
        runSuspended {
            channel.basicConsume(
                eventQueue,
                true,
                { _: String?, delivery: Delivery ->
                    GlobalScope.launch {
                        val json = decodeToString("application/octet-stream", delivery.body)
                        val event: AmqpEvent = Const.JSON.decodeFromString(json)
                        event.event?.let {
                            val shardEvent = ShardEvent(it, gateway, event.shardId)
                            events.emit(shardEvent)
                        }
                    }
                },
                { _: String? -> })
        }
    }
}

private suspend fun <T> runSuspended(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    body: suspend CoroutineScope.() -> T
): T = withContext(dispatcher, body)