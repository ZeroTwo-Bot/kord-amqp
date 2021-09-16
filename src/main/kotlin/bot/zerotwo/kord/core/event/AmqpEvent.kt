package bot.zerotwo.kord.core.event

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.gateway.ShardEvent
import dev.kord.gateway.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


val dev.kord.core.event.Event.gateway: Gateway get() = this.kord.gateway.gateways.getValue(0)

@Serializable
data class AmqpEvent(@SerialName("shard_id") val shardId: Int, @Serializable(Event.Companion::class) val event: Event?)



fun dev.kord.core.event.Event.toGuildId(): Snowflake? {
    return this.guildId
}

fun ShardEvent.getGuildId(): Snowflake? {
    return when (this.event) {
        is ChannelCreate -> (event as ChannelCreate).channel.guildId.value
        is ChannelUpdate -> (event as ChannelUpdate).channel.guildId.value
        is ChannelDelete -> (event as ChannelDelete).channel.guildId.value
        is ChannelPinsUpdate -> (event as ChannelPinsUpdate).pins.guildId.value
        is TypingStart -> (event as TypingStart).data.guildId.value
        is GuildCreate -> (event as GuildCreate).guild.id
        is GuildUpdate -> (event as GuildUpdate).guild.id
        is GuildDelete -> (event as GuildDelete).guild.id
        is GuildBanAdd -> (event as GuildBanAdd).ban.guildId
        is GuildBanRemove -> (event as GuildBanRemove).ban.guildId
        is GuildEmojisUpdate -> (event as GuildEmojisUpdate).emoji.guildId
        is GuildIntegrationsUpdate -> (event as GuildIntegrationsUpdate).integrations.guildId
        is GuildMemberAdd -> (event as GuildMemberAdd).member.guildId
        is GuildMemberRemove -> (event as GuildMemberRemove).member.guildId
        is GuildMemberUpdate -> (event as GuildMemberUpdate).member.guildId
        is GuildRoleCreate -> (event as GuildRoleCreate).role.guildId
        is GuildRoleUpdate -> (event as GuildRoleUpdate).role.guildId
        is GuildRoleDelete -> (event as GuildRoleDelete).role.guildId
        is GuildMembersChunk -> (event as GuildMembersChunk).data.guildId
        is InviteCreate -> (event as InviteCreate).invite.guildId.value
        is InviteDelete -> (event as InviteDelete).invite.guildId
        is MessageCreate -> (event as MessageCreate).message.guildId.value
        is MessageUpdate -> (event as MessageUpdate).message.guildId.value
        is MessageDelete -> (event as MessageDelete).message.guildId.value
        is MessageDeleteBulk -> (event as MessageDeleteBulk).messageBulk.guildId.value
        is MessageReactionAdd -> (event as MessageReactionAdd).reaction.guildId.value
        is MessageReactionRemove -> (event as MessageReactionRemove).reaction.guildId.value
        is MessageReactionRemoveAll -> (event as MessageReactionRemoveAll).reactions.guildId.value
        is MessageReactionRemoveEmoji -> (event as MessageReactionRemoveEmoji).reaction.guildId
        is PresenceUpdate -> (event as PresenceUpdate).presence.guildId.value
        is VoiceStateUpdate -> (event as VoiceStateUpdate).voiceState.guildId.value
        is VoiceServerUpdate -> (event as VoiceServerUpdate).voiceServerUpdateData.guildId
        is WebhooksUpdate -> (event as WebhooksUpdate).webhooksUpdateData.guildId
        is InteractionCreate -> (event as InteractionCreate).interaction.guildId.value
        is ApplicationCommandCreate -> (event as ApplicationCommandCreate).application.guildId.value
        is ApplicationCommandUpdate -> (event as ApplicationCommandUpdate).application.guildId.value
        is ApplicationCommandDelete -> (event as ApplicationCommandDelete).application.guildId.value
        is ThreadCreate -> (event as ThreadCreate).channel.guildId.value
        is ThreadUpdate -> (event as ThreadUpdate).channel.guildId.value
        is ThreadDelete -> (event as ThreadDelete).channel.guildId.value
        is ThreadListSync -> (event as ThreadListSync).sync.guildId
        is ThreadMembersUpdate -> (event as ThreadMembersUpdate).members.guildId
        else -> null
    }
}