package bot.zerotwo.kord.core.event

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.channel.*
import dev.kord.core.event.channel.thread.*
import dev.kord.core.event.guild.*
import dev.kord.core.event.interaction.ApplicationCommandCreateEvent
import dev.kord.core.event.interaction.ApplicationCommandDeleteEvent
import dev.kord.core.event.interaction.ApplicationCommandUpdateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.*
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import dev.kord.core.event.role.RoleUpdateEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.gateway.ShardEvent
import dev.kord.gateway.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


val dev.kord.core.event.Event.gateway: Gateway get() = this.kord.gateway.gateways.getValue(0)

@Serializable
data class AmqpEvent(@SerialName("shard_id") val shardId: Int, @Serializable(EventSerializer::class) val event: Event?)

object EventSerializer : KSerializer<Event?> {

    override val descriptor: SerialDescriptor
        get() = Event.descriptor

    override fun deserialize(decoder: Decoder): Event? {
        return Event.Companion.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: Event?) {
        error("not implemented")
    }

}

fun dev.kord.core.event.Event.toGuildId(): Snowflake? {
    return when (this) {
        is ChannelCreateEvent -> this.channel.data.guildId.value
        is ChannelUpdateEvent -> this.channel.data.guildId.value
        is ChannelDeleteEvent -> this.channel.data.guildId.value
        is ChannelPinsUpdateEvent -> this.guildId
        is TypingStartEvent -> this.guildId
        is GuildCreateEvent -> this.guild.id
        is GuildUpdateEvent -> this.guild.id
        is GuildDeleteEvent -> this.guildId
        is BanAddEvent -> this.guildId
        is BanRemoveEvent -> this.guildId
        is EmojisUpdateEvent -> this.guildId
        is IntegrationsUpdateEvent -> this.guildId
        is MemberJoinEvent -> this.guildId
        is MemberLeaveEvent -> this.guildId
        is MemberUpdateEvent -> this.guildId
        is RoleCreateEvent -> this.guildId
        is RoleUpdateEvent -> this.guildId
        is RoleDeleteEvent -> this.guildId
        is MembersChunkEvent -> this.guildId
        is InviteCreateEvent -> this.guildId
        is InviteDeleteEvent -> this.guildId
        is MessageCreateEvent -> this.guildId
        is MessageUpdateEvent -> null // guildId!!!! todo
        is MessageDeleteEvent -> this.guildId
        is MessageBulkDeleteEvent -> this.guildId
        is ReactionAddEvent -> this.guildId
        is ReactionRemoveEvent -> this.guildId
        is ReactionRemoveAllEvent -> this.guildId
        is ReactionRemoveEmojiEvent -> this.guildId
        is PresenceUpdateEvent -> this.guildId
        is VoiceStateUpdateEvent -> this.state.guildId
        is VoiceServerUpdateEvent -> this.guildId
        is WebhookUpdateEvent -> this.guildId
        is InteractionCreateEvent -> this.interaction.data.guildId.value
        is ApplicationCommandCreateEvent -> this.command.guildId
        is ApplicationCommandUpdateEvent -> this.command.guildId
        is ApplicationCommandDeleteEvent -> this.command.guildId
        is ThreadChannelCreateEvent -> this.channel.guildId
        is ThreadUpdateEvent -> this.channel.guildId // naming inconsistency
        is ThreadChannelDeleteEvent -> this.channel.guildId
        is ThreadListSyncEvent -> this.guildId
        is ThreadMembersUpdateEvent -> this.guildId
        else -> null
    }
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