package bot.zerotwo.kord.cache

import bot.zerotwo.kord.amqp.*
import bot.zerotwo.kord.core.ContextKeys
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.*
import dev.kord.core.entity.application.ApplicationCommandPermissions
import dev.kord.core.entity.application.GlobalApplicationCommand
import dev.kord.core.entity.application.GuildApplicationCommand
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.entity.channel.thread.ThreadMember
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

class AmqpCacheStrategy(
    private val amqp: AmqpWrapper,
    private val fallbackDelegate: EntitySupplyStrategy<EntitySupplier>?,
    private val totalShards: Int
) : EntitySupplyStrategy<EntitySupplier> {

    override fun supply(kord: Kord): EntitySupplier {
        return AtMostOncePerEventContextCacheSupplier(
            AmqpCacheSupplier(
                kord,
                amqp,
                fallbackDelegate?.supply(kord),
                totalShards
            )
        )
    }


}

class AtMostOncePerEventContextCacheSupplier(private val supplier: EntitySupplier) : EntitySupplier {
    override val guilds: Flow<Guild>
        get() = flow {
            val key = getKey("guilds")
            val contextData = getContextData<List<Guild>>(key)
            if (contextData == null) {
                val guilds = mutableListOf<Guild>()
                supplier.guilds
                    .toList(guilds)
                setContextData(key, guilds)
                this.emitAll(guilds.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    override val regions: Flow<Region>
        get() = flow {
            val key = getKey("regions")
            val contextData = getContextData<List<Region>>(key)
            if (contextData == null) {
                val regions = mutableListOf<Region>()
                supplier.regions
                    .toList(regions)
                setContextData(key, regions)
                this.emitAll(regions.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }

    private fun getKey(type: String, vararg ids: Snowflake): String {
        val key = StringBuilder(type)
        ids.forEach { key.append(".").append(it.asString) }
        return key.toString()
    }


    override fun getActiveThreads(guildId: Snowflake): Flow<ThreadChannel> {
        return flow {
            val key = getKey("threads", guildId)
            val contextData = getContextData<List<ThreadChannel>>(key)
            if (contextData == null) {
                val channels = mutableListOf<ThreadChannel>()
                supplier.getActiveThreads(guildId)
                    .toList(channels)
                setContextData(key, channels)
                this.emitAll(channels.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getApplicationCommandPermissionsOrNull(
        applicationId: Snowflake,
        guildId: Snowflake,
        commandId: Snowflake
    ): ApplicationCommandPermissions? {
        val key = getKey("appCmdPerms", applicationId, guildId, commandId)
        val contextData = getContextData<ApplicationCommandPermissions>(key)
        return if (contextData == null) {
            val data = supplier.getApplicationCommandPermissionsOrNull(applicationId, guildId, commandId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getChannelOrNull(id: Snowflake): Channel? {
        val key = getKey("channel", id)
        val contextData = getContextData<Channel>(key)
        return if (contextData == null) {
            val data = supplier.getChannelOrNull(id)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getChannelPins(channelId: Snowflake): Flow<Message> {
        return flow {
            val key = getKey("channelPins", channelId)
            val contextData = getContextData<List<Message>>(key)
            if (contextData == null) {
                val msgs = mutableListOf<Message>()
                supplier.getChannelPins(channelId)
                    .toList(msgs)
                setContextData(key, msgs)
                this.emitAll(msgs.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getChannelWebhooks(channelId: Snowflake): Flow<Webhook> {
        return flow {
            val key = getKey("channelWebhooks", channelId)
            val contextData = getContextData<List<Webhook>>(key)
            if (contextData == null) {
                val webhooks = mutableListOf<Webhook>()
                supplier.getChannelWebhooks(channelId)
                    .toList(webhooks)
                setContextData(key, webhooks)
                this.emitAll(webhooks.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getCurrentUserGuilds(limit: Int): Flow<Guild> {
        return supplier.getCurrentUserGuilds(limit)
    }

    override suspend fun getEmojiOrNull(guildId: Snowflake, emojiId: Snowflake): GuildEmoji? {
        val key = getKey("channel", guildId, emojiId)
        val contextData = getContextData<GuildEmoji>(key)
        return if (contextData == null) {
            val data = supplier.getEmojiOrNull(guildId, emojiId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getEmojis(guildId: Snowflake): Flow<GuildEmoji> {
        return flow {
            val key = getKey("emojis", guildId)
            val contextData = getContextData<List<GuildEmoji>>(key)
            if (contextData == null) {
                val emojis = mutableListOf<GuildEmoji>()
                supplier.getEmojis(guildId)
                    .toList(emojis)
                setContextData(key, emojis)
                this.emitAll(emojis.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getGlobalApplicationCommandOrNull(
        applicationId: Snowflake,
        commandId: Snowflake
    ): GlobalApplicationCommand? {
        val key = getKey("globalAppCmd", applicationId, commandId)
        val contextData = getContextData<GlobalApplicationCommand>(key)
        return if (contextData == null) {
            val data = supplier.getGlobalApplicationCommandOrNull(applicationId, commandId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getGlobalApplicationCommands(applicationId: Snowflake): Flow<GlobalApplicationCommand> {
        return flow {
            val key = getKey("globalAppCdms", applicationId)
            val contextData = getContextData<List<GlobalApplicationCommand>>(key)
            if (contextData == null) {
                val cmds = mutableListOf<GlobalApplicationCommand>()
                supplier.getGlobalApplicationCommands(applicationId)
                    .toList(cmds)
                setContextData(key, cmds)
                this.emitAll(cmds.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getGuildApplicationCommandOrNull(
        applicationId: Snowflake,
        guildId: Snowflake,
        commandId: Snowflake
    ): GuildApplicationCommand? {
        val key = getKey("guildAppCmd", applicationId, guildId, commandId)
        val contextData = getContextData<GuildApplicationCommand>(key)
        return if (contextData == null) {
            val data = supplier.getGuildApplicationCommandOrNull(applicationId, guildId, commandId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getGuildApplicationCommandPermissions(
        applicationId: Snowflake,
        guildId: Snowflake
    ): Flow<ApplicationCommandPermissions> {
        return flow {
            val key = getKey("guildAppCmdPerms", applicationId, guildId)
            val contextData = getContextData<List<ApplicationCommandPermissions>>(key)
            if (contextData == null) {
                val perms = mutableListOf<ApplicationCommandPermissions>()
                supplier.getGuildApplicationCommandPermissions(applicationId, guildId)
                    .toList(perms)
                setContextData(key, perms)
                this.emitAll(perms.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getGuildApplicationCommands(
        applicationId: Snowflake,
        guildId: Snowflake
    ): Flow<GuildApplicationCommand> {
        return flow {
            val key = getKey("guildAppCmds", applicationId, guildId)
            val contextData = getContextData<List<GuildApplicationCommand>>(key)
            if (contextData == null) {
                val cmds = mutableListOf<GuildApplicationCommand>()
                supplier.getGuildApplicationCommands(applicationId, guildId)
                    .toList(cmds)
                setContextData(key, cmds)
                this.emitAll(cmds.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getGuildBanOrNull(guildId: Snowflake, userId: Snowflake): Ban? {
        val key = getKey("ban", guildId, userId)
        val contextData = getContextData<Ban>(key)
        return if (contextData == null) {
            val data = supplier.getGuildBanOrNull(guildId, userId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getGuildBans(guildId: Snowflake): Flow<Ban> {
        return flow {
            val key = getKey("bans", guildId)
            val contextData = getContextData<List<Ban>>(key)
            if (contextData == null) {
                val bans = mutableListOf<Ban>()
                supplier.getGuildBans(guildId)
                    .toList(bans)
                setContextData(key, bans)
                this.emitAll(bans.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getGuildChannels(guildId: Snowflake): Flow<TopGuildChannel> {
        return flow {
            val key = getKey("guildChannels", guildId)
            val contextData = getContextData<List<TopGuildChannel>>(key)
            if (contextData == null) {
                val channels = mutableListOf<TopGuildChannel>()
                supplier.getGuildChannels(guildId)
                    .toList(channels)
                setContextData(key, channels)
                this.emitAll(channels.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getGuildMembers(guildId: Snowflake, limit: Int): Flow<Member> {
        return this.getGuildMembers(guildId, limit)
    }

    override suspend fun getGuildOrNull(id: Snowflake): Guild? {
        val key = getKey("guild", id)
        val contextData = getContextData<Guild>(key)
        return if (contextData == null) {
            val data = supplier.getGuildOrNull(id)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getGuildPreviewOrNull(guildId: Snowflake): GuildPreview? {
        val key = getKey("preview", guildId)
        val contextData = getContextData<GuildPreview>(key)
        return if (contextData == null) {
            val data = supplier.getGuildPreviewOrNull(guildId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getGuildRoles(guildId: Snowflake): Flow<Role> {
        return flow {
            val key = getKey("roles", guildId)
            val contextData = getContextData<List<Role>>(key)
            if (contextData == null) {
                val roles = mutableListOf<Role>()
                supplier.getGuildRoles(guildId)
                    .toList(roles)
                setContextData(key, roles)
                this.emitAll(roles.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getGuildVoiceRegions(guildId: Snowflake): Flow<Region> {
        return flow {
            val key = getKey("regions", guildId)
            val contextData = getContextData<List<Region>>(key)
            if (contextData == null) {
                val regions = mutableListOf<Region>()
                supplier.getGuildVoiceRegions(guildId)
                    .toList(regions)
                setContextData(key, regions)
                this.emitAll(regions.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override fun getGuildWebhooks(guildId: Snowflake): Flow<Webhook> {
        return flow {
            val key = getKey("guildWebhooks", guildId)
            val contextData = getContextData<List<Webhook>>(key)
            if (contextData == null) {
                val webhooks = mutableListOf<Webhook>()
                supplier.getGuildWebhooks(guildId)
                    .toList(webhooks)
                setContextData(key, webhooks)
                this.emitAll(webhooks.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getGuildWidgetOrNull(guildId: Snowflake): GuildWidget? {
        val key = getKey("widget", guildId)
        val contextData = getContextData<GuildWidget>(key)
        return if (contextData == null) {
            val data = supplier.getGuildWidgetOrNull(guildId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getJoinedPrivateArchivedThreads(
        channelId: Snowflake,
        before: Snowflake,
        limit: Int
    ): Flow<ThreadChannel> {
        return supplier.getJoinedPrivateArchivedThreads(channelId, before, limit)
    }

    override suspend fun getMemberOrNull(guildId: Snowflake, userId: Snowflake): Member? {
        val key = getKey("member", guildId, userId)
        val contextData = getContextData<Member>(key)
        return if (contextData == null) {
            val data = supplier.getMemberOrNull(guildId, userId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getMessageOrNull(channelId: Snowflake, messageId: Snowflake): Message? {
        val key = getKey("message", channelId, messageId)
        val contextData = getContextData<Message>(key)
        return if (contextData == null) {
            val data = supplier.getMessageOrNull(channelId, messageId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override fun getMessagesAfter(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return supplier.getMessagesAfter(messageId, channelId, limit)
    }

    override fun getMessagesAround(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return supplier.getMessagesAround(messageId, channelId, limit)
    }

    override fun getMessagesBefore(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return supplier.getMessagesBefore(messageId, channelId, limit)
    }

    override fun getPrivateArchivedThreads(channelId: Snowflake, before: Instant, limit: Int): Flow<ThreadChannel> {
        return supplier.getPrivateArchivedThreads(channelId, before, limit)
    }

    override fun getPublicArchivedThreads(channelId: Snowflake, before: Instant, limit: Int): Flow<ThreadChannel> {
        return supplier.getPublicArchivedThreads(channelId, before, limit)
    }

    override suspend fun getRoleOrNull(guildId: Snowflake, roleId: Snowflake): Role? {
        val key = getKey("role", guildId, roleId)
        val contextData = getContextData<Role>(key)
        return if (contextData == null) {
            val data = supplier.getRoleOrNull(guildId, roleId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getSelfOrNull(): User? {
        val key = getKey("selfUser")
        val contextData = getContextData<User>(key)
        return if (contextData == null) {
            val data = supplier.getSelfOrNull()
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getStageInstanceOrNull(channelId: Snowflake): StageInstance? {
        val key = getKey("stage", channelId)
        val contextData = getContextData<StageInstance>(key)
        return if (contextData == null) {
            val data = supplier.getStageInstanceOrNull(channelId)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getTemplateOrNull(code: String): Template? {
        return supplier.getTemplateOrNull(code)
    }

    override fun getTemplates(guildId: Snowflake): Flow<Template> {
        return supplier.getTemplates(guildId)
    }

    override fun getThreadMembers(channelId: Snowflake): Flow<ThreadMember> {
        return flow {
            val key = getKey("threadMembers", channelId)
            val contextData = getContextData<List<ThreadMember>>(key)
            if (contextData == null) {
                val members = mutableListOf<ThreadMember>()
                supplier.getThreadMembers(channelId)
                    .toList(members)
                setContextData(key, members)
                this.emitAll(members.asFlow())
            } else {
                this.emitAll(contextData.asFlow())
            }
        }
    }

    override suspend fun getUserOrNull(id: Snowflake): User? {
        val key = getKey("user", id)
        val contextData = getContextData<User>(key)
        return if (contextData == null) {
            val data = supplier.getUserOrNull(id)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getWebhookOrNull(id: Snowflake): Webhook? {
        val key = getKey("webhook", id)
        val contextData = getContextData<Webhook>(key)
        return if (contextData == null) {
            val data = supplier.getWebhookOrNull(id)
            setContextData(key, data)
            data
        } else {
            contextData
        }
    }

    override suspend fun getWebhookWithTokenOrNull(id: Snowflake, token: String): Webhook? {
        return supplier.getWebhookWithTokenOrNull(id, token)
    }
}

class AmqpCacheSupplier(
    private val kord: Kord,
    private val amqp: AmqpWrapper,
    private val fallbackDelegate: EntitySupplier?,
    private val totalShards: Int
) : EntitySupplier {

    private val maxMembersPerRequest = 5000;

    private fun getShardId(guildId: Snowflake): Int {
        return ((guildId.value.shr(22).toLong()) % this.totalShards).toInt()
    }

    override val guilds: Flow<Guild>
        get() = emptyFlow() // we dont get all guilds from amqp... that's a lotta traffic
    override val regions: Flow<Region>
        get() = emptyFlow() // wtf is that

    override fun getActiveThreads(guildId: Snowflake): Flow<ThreadChannel> {
        return flow {
            (amqp.getActiveThreads(getShardId(guildId), guildId)
                ?.asFlow()
                ?: emptyFlow())
                .mapNotNull { Channel.from(it.toData(), kord) as? ThreadChannel }
                .let { this.emitAll(it) }
        }
    }

    override suspend fun getApplicationCommandPermissionsOrNull(
        applicationId: Snowflake,
        guildId: Snowflake,
        commandId: Snowflake
    ): ApplicationCommandPermissions? {
        // todo cached?
        return fallbackDelegate?.getApplicationCommandPermissionsOrNull(applicationId, guildId, commandId)
    }

    override suspend fun getChannelOrNull(id: Snowflake): Channel? {
        val guildId = getGuildIdFromContext()
        return if (guildId != null) {
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            amqp.getChannel(shardId, guildId, id)
        } else {
            amqp.getChannel(0, Snowflake(0), id)
        }?.toData()?.let { Channel.from(it, kord) }
    }

    override fun getChannelPins(channelId: Snowflake): Flow<Message> {
        return emptyFlow() // we dont cache messages
    }

    override fun getChannelWebhooks(channelId: Snowflake): Flow<Webhook> {
        // todo cached?
        return fallbackDelegate?.getChannelWebhooks(channelId) ?: emptyFlow()
    }

    override fun getCurrentUserGuilds(limit: Int): Flow<Guild> {
        return emptyFlow() // No self user guilds
    }

    override suspend fun getEmojiOrNull(guildId: Snowflake, emojiId: Snowflake): GuildEmoji? {
        val shardId = getShardIdFromContext() ?: getShardId(guildId)
        return amqp.getEmoji(shardId, guildId, emojiId)
            ?.let { GuildEmoji(it.toData(guildId, emojiId), kord) }
    }

    override fun getEmojis(guildId: Snowflake): Flow<GuildEmoji> {
        return flow {
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            (amqp.getEmojis(shardId, guildId)
                ?.asFlow()
                ?: emptyFlow())
                .map { GuildEmoji(it.toData(guildId, it.id!!), kord) }
                .let { this.emitAll(it) }
        }
    }

    override suspend fun getGlobalApplicationCommandOrNull(
        applicationId: Snowflake,
        commandId: Snowflake
    ): GlobalApplicationCommand? {
        // todo cached?
        return fallbackDelegate?.getGlobalApplicationCommandOrNull(applicationId, commandId)
    }

    override fun getGlobalApplicationCommands(applicationId: Snowflake): Flow<GlobalApplicationCommand> {
        // todo cached?
        return fallbackDelegate?.getGlobalApplicationCommands(applicationId) ?: emptyFlow()
    }

    override suspend fun getGuildApplicationCommandOrNull(
        applicationId: Snowflake,
        guildId: Snowflake,
        commandId: Snowflake
    ): GuildApplicationCommand? {
        // todo cached?
        return fallbackDelegate?.getGuildApplicationCommandOrNull(applicationId, guildId, commandId)
    }

    override fun getGuildApplicationCommandPermissions(
        applicationId: Snowflake,
        guildId: Snowflake
    ): Flow<ApplicationCommandPermissions> {
        // todo cached?
        return fallbackDelegate?.getGuildApplicationCommandPermissions(applicationId, guildId) ?: emptyFlow()
    }

    override fun getGuildApplicationCommands(
        applicationId: Snowflake,
        guildId: Snowflake
    ): Flow<GuildApplicationCommand> {
        // todo cached?
        return fallbackDelegate?.getGuildApplicationCommands(applicationId, guildId) ?: emptyFlow()
    }

    override suspend fun getGuildBanOrNull(guildId: Snowflake, userId: Snowflake): Ban? {
        return fallbackDelegate?.getGuildBanOrNull(guildId, userId)
    }

    override fun getGuildBans(guildId: Snowflake): Flow<Ban> {
        return fallbackDelegate?.getGuildBans(guildId) ?: emptyFlow()
    }

    override fun getGuildChannels(guildId: Snowflake): Flow<TopGuildChannel> {
        return flow {
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            (amqp.getChannels(shardId, guildId)
                ?.asFlow()
                ?: emptyFlow())
                .mapNotNull { Channel.from(it.toData(), kord) as? TopGuildChannel }
                .let { this.emitAll(it) }
        }
    }

    override fun getGuildMembers(guildId: Snowflake, limit: Int): Flow<Member> {
        return flow {
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            if (limit <= maxMembersPerRequest) {
                (amqp.getMembers(shardId, guildId, null, null, limit)
                    ?.asFlow()
                    ?: emptyFlow())
                    .map { Member(it.toData(it.user.value!!.id, guildId), it.user.value!!.toData(), kord) }
                    .let { this.emitAll(it) }
            } else {
                var start: Snowflake? = null
                var lastMemberResult = maxMembersPerRequest
                var total = 0
                while (lastMemberResult == maxMembersPerRequest) {
                    val innerLimit = kotlin.math.min(limit - total, maxMembersPerRequest)
                    val members = (amqp.getMembers(shardId, guildId, start, null, innerLimit)
                        ?.asFlow()
                        ?: emptyFlow())
                        .map { Member(it.toData(it.user.value!!.id, guildId), it.user.value!!.toData(), kord) }
                        .toList()
                    start = members.maxByOrNull { it.id.value }?.id ?: Snowflake.max
                    lastMemberResult = members.size
                    total += members.size
                    this.emitAll(members.asFlow())
                }
            }
        }
    }

    override suspend fun getGuildOrNull(id: Snowflake): Guild? {
        val shardId = getShardIdFromContext() ?: getShardId(id)
        return amqp.getGuild(shardId, id)
            ?.let { Guild(it.toData(), kord) }
    }

    override suspend fun getGuildPreviewOrNull(guildId: Snowflake): GuildPreview? {
        return fallbackDelegate?.getGuildPreviewOrNull(guildId)
    }

    override fun getGuildRoles(guildId: Snowflake): Flow<Role> {
        return flow {
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            (amqp.getRoles(shardId, guildId)
                ?.asFlow()
                ?: emptyFlow())
                .map { Role(it.toData(guildId), kord) }
                .let { this.emitAll(it) }
        }
    }

    override fun getGuildVoiceRegions(guildId: Snowflake): Flow<Region> {
        // todo cached?
        return fallbackDelegate?.getGuildVoiceRegions(guildId) ?: emptyFlow()
    }

    override fun getGuildWebhooks(guildId: Snowflake): Flow<Webhook> {
        // todo cached?
        return fallbackDelegate?.getGuildWebhooks(guildId) ?: emptyFlow()
    }

    override suspend fun getGuildWidgetOrNull(guildId: Snowflake): GuildWidget? {
        return fallbackDelegate?.getGuildWidgetOrNull(guildId)
    }

    override fun getJoinedPrivateArchivedThreads(
        channelId: Snowflake,
        before: Snowflake,
        limit: Int
    ): Flow<ThreadChannel> {
        TODO("Do we need that?")
    }

    override suspend fun getMemberOrNull(guildId: Snowflake, userId: Snowflake): Member? {
        val shardId = getShardIdFromContext() ?: getShardId(guildId)
        return amqp.getMember(shardId, guildId, userId)
            ?.let { Member(it.toData(userId, guildId), it.user.value!!.toData(), kord) }
    }

    override suspend fun getMessageOrNull(channelId: Snowflake, messageId: Snowflake): Message? {
        return fallbackDelegate?.getMessageOrNull(channelId, messageId)
    }

    override fun getMessagesAfter(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return fallbackDelegate?.getMessagesAfter(messageId, channelId, limit) ?: emptyFlow()
    }

    override fun getMessagesAround(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return fallbackDelegate?.getMessagesAround(messageId, channelId, limit) ?: emptyFlow()
    }

    override fun getMessagesBefore(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        return fallbackDelegate?.getMessagesBefore(messageId, channelId, limit) ?: emptyFlow()
    }

    override fun getPrivateArchivedThreads(channelId: Snowflake, before: Instant, limit: Int): Flow<ThreadChannel> {
        TODO("Do we need that?")
    }

    override fun getPublicArchivedThreads(channelId: Snowflake, before: Instant, limit: Int): Flow<ThreadChannel> {
        TODO("Do we need that?")
    }

    override suspend fun getRoleOrNull(guildId: Snowflake, roleId: Snowflake): Role? {
        val shardId = getShardIdFromContext() ?: getShardId(guildId)
        return amqp.getRole(shardId, guildId, roleId)
            ?.let { Role(it.toData(guildId), kord) }
    }

    override suspend fun getSelfOrNull(): User? {
        val botId = getBotId() ?: kord.selfId
        return amqp.getUser(0, botId)
            ?.let { User(it.toData(), kord) }
    }

    override suspend fun getStageInstanceOrNull(channelId: Snowflake): StageInstance? {
        val guildId = getGuildIdFromContext()!!
        val shardId = getShardIdFromContext() ?: getShardId(guildId)
        return amqp.getStageInstance(shardId, guildId, channelId)
            ?.let { StageInstance(it.toData(), kord, this) } // todo remove supplier
    }

    override suspend fun getTemplateOrNull(code: String): Template? {
        return fallbackDelegate?.getTemplateOrNull(code)
    }

    override fun getTemplates(guildId: Snowflake): Flow<Template> {
        return fallbackDelegate?.getTemplates(guildId) ?: emptyFlow()
    }

    override fun getThreadMembers(channelId: Snowflake): Flow<ThreadMember> {
        return flow {
            val guildId = getGuildIdFromContext()!!
            val shardId = getShardIdFromContext() ?: getShardId(guildId)
            (amqp.getThreadMembers(shardId, guildId, channelId)
                ?.asFlow()
                ?: emptyFlow())
                .map { ThreadMember(it.toData(channelId), kord) }
                .let { this.emitAll(it) }
        }
    }

    override suspend fun getUserOrNull(id: Snowflake): User? {
        TODO("Find User")
    }

    override suspend fun getWebhookOrNull(id: Snowflake): Webhook? {
        // todo cached?
        return fallbackDelegate?.getWebhookOrNull(id)
    }

    override suspend fun getWebhookWithTokenOrNull(id: Snowflake, token: String): Webhook? {
        // todo cached?
        return fallbackDelegate?.getWebhookWithTokenOrNull(id, token)
    }
}

private suspend fun getBotId(): Snowflake? {
    return currentCoroutineContext()[ContextKeys.REQUEST_META_KEY]?.botId
}

private suspend fun getGuildIdFromContext(): Snowflake? {
    return currentCoroutineContext()[ContextKeys.REQUEST_META_KEY]?.guildId
}

private suspend fun getShardIdFromContext(): Int? {
    return currentCoroutineContext()[ContextKeys.REQUEST_META_KEY]?.shardId
}

private suspend fun <T> getContextData(key: String): T? {
    return currentCoroutineContext()[ContextKeys.EVENT_CACHE]?.get<T>(key)
}

private suspend fun setContextData(key: String, data: Any?) {
    currentCoroutineContext()[ContextKeys.EVENT_CACHE]?.push(key, data)
}