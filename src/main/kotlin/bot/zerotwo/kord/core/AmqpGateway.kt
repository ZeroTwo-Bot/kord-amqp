@file:OptIn(ExperimentalTime::class, PrivilegedIntent::class)

package bot.zerotwo.kord.core

import bot.zerotwo.kord.amqp.AmqpRequest
import bot.zerotwo.kord.amqp.AmqpWrapper
import bot.zerotwo.kord.core.event.getGuildId
import dev.kord.core.Kord
import dev.kord.core.gateway.MasterGateway
import dev.kord.core.gateway.ShardEvent
import dev.kord.gateway.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class AmqpMasterGateway(
    internal val amqp: AmqpWrapper,
    private val shardEventFlow: MutableSharedFlow<ShardEvent>,
    override val gateways: Map<Int, Gateway>
) : MasterGateway {

    override val averagePing: Duration?
        get() = Duration.ZERO
    override val events: Flow<ShardEvent>
        get() = shardEventFlow

}

class AmqpGateway(private val amqp: AmqpWrapper) : Gateway {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default
    override val events: SharedFlow<Event>
        get() = MutableSharedFlow()
    override val ping: StateFlow<Duration?>
        get() = MutableStateFlow(Duration.ZERO)

    override suspend fun detach() {
    }

    override suspend fun send(command: Command) {
        when (command) {
            is RequestGuildMembers -> throw UnsupportedOperationException("Client should not request members via gateway!")
            is UpdateVoiceStatus -> {
                val shard = getShardByGuildId(guildId = command.guildId)
                amqp.request(shard, AmqpRequest.UpdateVoiceState(command))
            }
            is UpdateStatus -> amqp.request(0, AmqpRequest.UpdatePresence(command)) // todo fanout/shardid
            else -> {
            }
        }
    }

    override suspend fun start(configuration: GatewayConfiguration) {
    }

    override suspend fun stop() {
    }
}