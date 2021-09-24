package bot.zerotwo.kord.amqp

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.UpdateStatus
import dev.kord.gateway.UpdateVoiceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class AmqpRequest(
    @SerialName("t")
    val type: Int
) {
    @Serializable class GetUser(@SerialName("d") val data: ById) : AmqpRequest(1)
    @Serializable class GetGuild(@SerialName("d") val data: ById) : AmqpRequest(2)
    @Serializable class GetMember(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(3)
    @Serializable class GetMembers(@SerialName("d") val data: ByGuildIdPaginated) : AmqpRequest(4)
    @Serializable class GetChannel(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(5)
    @Serializable class GetChannels(@SerialName("d") val data: ByGuildId) : AmqpRequest(6)
    @Serializable class GetThread(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(7)
    @Serializable class GetThreads(@SerialName("d") val data: ByGuildId) : AmqpRequest(8)
    @Serializable class GetRole(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(9)
    @Serializable class GetRoles(@SerialName("d") val data: ByGuildId) : AmqpRequest(10)
    @Serializable class GetEmoji(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(11)
    @Serializable class GetEmojis(@SerialName("d") val data: ByGuildId) : AmqpRequest(12)
    @Serializable class GetStageInstance(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(13)
    @Serializable class GetStageInstances(@SerialName("d") val data: ByGuildId) : AmqpRequest(14)
    @Serializable class GetUsers(@SerialName("d") val data: BySnowflakes) : AmqpRequest(15)
    @Serializable class GetThreadMembers(@SerialName("d") val data: ByGuildIdAndId) : AmqpRequest(50)
    @Serializable class UpdatePresence(@SerialName("d") val data: UpdateStatus) : AmqpRequest(81)
    @Serializable class UpdateVoiceState(@SerialName("d") val data: UpdateVoiceStatus) : AmqpRequest(82)
    @Serializable class GetStats : AmqpRequest(99)

}

@Serializable
abstract class AmqpRequestData {

}

@Serializable
data class ById(val id: String): AmqpRequestData()

@Serializable
data class ByGuildId(@SerialName("gid") val guildId: String): AmqpRequestData()

@Serializable
data class ByGuildIdAndId(val id: String, @SerialName("gid") val guildId: String): AmqpRequestData()

@Serializable
data class ByGuildIdPaginated(@SerialName("gid") val guild_id: String, val start: String? = null, val end: String? = null, val limit: Int? = null) : AmqpRequestData()

@Serializable
data class BySnowflakes(@SerialName("ids") val snowflakes: List<Snowflake>) : AmqpRequestData()