@file:OptIn(ExperimentalSerializationApi::class)
package bot.zerotwo.kord.amqp

import dev.kord.common.entity.*
import dev.kord.common.entity.optional.*
import dev.kord.core.cache.data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

suspend fun AmqpWrapper.getUser(shardId: Int, userId: Snowflake): DiscordUser? {
    val json = request(
        shardId,
        AmqpRequest.GetUser(ById(userId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getGuild(shardId: Int, guildId: Snowflake): SnowflakedDiscordGuild? {
    val json = request(
        shardId,
        AmqpRequest.GetGuild(ById(guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getMember(shardId: Int, guildId: Snowflake, memberId: Snowflake): DiscordGuildMember? {
    val json = request(
        shardId,
        AmqpRequest.GetMember(ByGuildIdAndId(guildId = guildId.asString, id = memberId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getMembers(
    shardId: Int,
    guildId: Snowflake,
    start: Snowflake? = null,
    end: Snowflake? = null,
    limit: Int? = null
): Array<DiscordGuildMember>? {
    val json = request(
        shardId,
        AmqpRequest.GetMembers(
            ByGuildIdPaginated(
                guild_id = guildId.asString,
                start = start?.asString,
                end = end?.asString,
                limit
            )
        )
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getChannel(shardId: Int, guildId: Snowflake, channelId: Snowflake): DiscordChannel? {
    val json = request(
        shardId,
        AmqpRequest.GetChannel(ByGuildIdAndId(guildId = guildId.asString, id = channelId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getChannels(shardId: Int, guildId: Snowflake): Array<DiscordChannel>? {
    val json = request(
        shardId,
        AmqpRequest.GetChannels(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getThread(shardId: Int, guildId: Snowflake, threadId: Snowflake): DiscordChannel? {
    val json = request(
        shardId,
        AmqpRequest.GetThread(ByGuildIdAndId(guildId = guildId.asString, id = threadId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getActiveThreads(shardId: Int, guildId: Snowflake): Array<DiscordChannel>? {
    val json = request(
        shardId,
        AmqpRequest.GetThreads(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getArchivedThreads(shardId: Int, guildId: Snowflake): Array<DiscordChannel>? {
    val json = request(
        shardId,
        AmqpRequest.GetThreads(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getRole(shardId: Int, guildId: Snowflake, roleId: Snowflake): DiscordRole? {
    val json = request(
        shardId,
        AmqpRequest.GetRole(ByGuildIdAndId(guildId = guildId.asString, id = roleId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getRoles(shardId: Int, guildId: Snowflake): Array<DiscordRole>? {
    val json = request(
        shardId,
        AmqpRequest.GetRoles(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(it) }
}

suspend fun AmqpWrapper.getEmoji(shardId: Int, guildId: Snowflake, emojiId: Snowflake): DiscordEmoji? {
    val json = request(
        shardId,
        AmqpRequest.GetEmoji(ByGuildIdAndId(guildId = guildId.asString, id = emojiId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(json) }
}

suspend fun AmqpWrapper.getEmojis(shardId: Int, guildId: Snowflake): Array<DiscordEmoji>? {
    val json = request(
        shardId,
        AmqpRequest.GetEmojis(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(json) }
}

suspend fun AmqpWrapper.getStageInstance(shardId: Int, guildId: Snowflake, id: Snowflake): DiscordStageInstance? {
    val json = request(
        shardId,
        AmqpRequest.GetStageInstance(ByGuildIdAndId(guildId = guildId.asString, id = id.asString))
    )
    return json?.let { Const.JSON.decodeFromString(json) }
}

suspend fun AmqpWrapper.getStageInstances(shardId: Int, guildId: Snowflake): Array<DiscordStageInstance>? {
    val json = request(
        shardId,
        AmqpRequest.GetStageInstances(ByGuildId(guildId = guildId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(json) }
}

suspend fun AmqpWrapper.getThreadMembers(shardId: Int, guildId: Snowflake, threadId: Snowflake): Array<DiscordThreadMember>? {
    val json = request(
        shardId,
        AmqpRequest.GetThreadMembers(ByGuildIdAndId(guildId = guildId.asString, id = threadId.asString))
    )
    return json?.let { Const.JSON.decodeFromString(json) }
}

suspend fun AmqpWrapper.getStats(shardId: Int): TotalStats? {
    val json = request(shardId, AmqpRequest.GetStats())
    return json?.let { Const.JSON.decodeFromString(it) }
}

@Serializable
data class SnowflakedDiscordGuild(
    val id: Snowflake,
    val name: String,
    val icon: String?,
    @SerialName("icon_hash")
    val iconHash: Optional<String?> = Optional.Missing(),
    val splash: Optional<String?> = Optional.Missing(),
    @SerialName("discovery_splash")
    val discoverySplash: Optional<String?> = Optional.Missing(),
    val owner: OptionalBoolean = OptionalBoolean.Missing,
    @SerialName("owner_id")
    val ownerId: Snowflake,
    val permissions: Optional<Permissions> = Optional.Missing(),
    @SerialName("afk_channel_id")
    val afkChannelId: Snowflake?,
    @SerialName("afk_timeout")
    val afkTimeout: Int,
    @SerialName("widget_enabled")
    val widgetEnabled: OptionalBoolean = OptionalBoolean.Missing,
    @SerialName("widget_channel_id")
    val widgetChannelId: OptionalSnowflake? = OptionalSnowflake.Missing,
    @SerialName("verification_level")
    val verificationLevel: VerificationLevel,
    @SerialName("default_message_notifications")
    val defaultMessageNotifications: DefaultMessageNotificationLevel,
    @SerialName("explicit_content_filter")
    val explicitContentFilter: ExplicitContentFilter,
    val roles: List<Snowflake>,
    val emojis: List<Snowflake>,
    val features: List<GuildFeature>,
    @SerialName("mfa_level")
    val mfaLevel: MFALevel,
    @SerialName("application_id")
    val applicationId: Snowflake?,
    @SerialName("system_channel_id")
    val systemChannelId: Snowflake?,
    @SerialName("system_channel_flags")
    val systemChannelFlags: SystemChannelFlags,
    @SerialName("rules_channel_id")
    val rulesChannelId: Snowflake?,
    @SerialName("joined_at")
    val joinedAt: Optional<String> = Optional.Missing(),
    val large: OptionalBoolean = OptionalBoolean.Missing,
    val unavailable: OptionalBoolean = OptionalBoolean.Missing,
    @SerialName("member_count")
    val memberCount: OptionalInt = OptionalInt.Missing,
    val channels: Optional<List<Snowflake>> = Optional.Missing(),
    val threads: Optional<List<ChannelData>> = Optional.Missing(),
    @SerialName("max_presences")
    val maxPresences: OptionalInt? = OptionalInt.Missing,
    @SerialName("max_members")
    val maxMembers: OptionalInt = OptionalInt.Missing,
    @SerialName("vanity_url_code")
    val vanityUrlCode: String?,
    val description: String?,
    val banner: String?,
    @SerialName("premium_tier")
    val premiumTier: PremiumTier,
    @SerialName("premium_subscription_count")
    val premiumSubscriptionCount: OptionalInt = OptionalInt.Missing,
    @SerialName("preferred_locale")
    val preferredLocale: String,
    @SerialName("public_updates_channel_id")
    val publicUpdatesChannelId: Snowflake?,
    @SerialName("max_video_channel_users")
    val maxVideoChannelUsers: OptionalInt = OptionalInt.Missing,
    @SerialName("approximate_member_count")
    val approximateMemberCount: OptionalInt = OptionalInt.Missing,
    @SerialName("approximate_presence_count")
    val approximatePresenceCount: OptionalInt = OptionalInt.Missing,
    @SerialName("welcome_screen")
    val welcomeScreen: Optional<DiscordWelcomeScreen> = Optional.Missing(),
    @SerialName("nsfw_level")
    val nsfwLevel: NsfwLevel
)

fun SnowflakedDiscordGuild.toData(): GuildData {
    return GuildData(
        id = this.id,
        name = this.name,
        icon = this.icon,
        iconHash = this.iconHash,
        splash= this.splash,
        discoverySplash = this.discoverySplash,
        ownerId = this.ownerId,
        permissions = this.permissions,
        region = "us-east", // todo: deprecated
        afkChannelId = this.afkChannelId,
        afkTimeout = this.afkTimeout,
        widgetEnabled = this.widgetEnabled,
        widgetChannelId = this.widgetChannelId,
        verificationLevel = this.verificationLevel,
        defaultMessageNotifications = this.defaultMessageNotifications,
        explicitContentFilter = this.explicitContentFilter,
        roles = this.roles,
        emojis = this.emojis,
        features = this.features,
        mfaLevel = this.mfaLevel,
        applicationId = this.applicationId,
        systemChannelId = this.systemChannelId,
        systemChannelFlags = this.systemChannelFlags,
        rulesChannelId = this.rulesChannelId,
        joinedAt = this.joinedAt,
        large = this.large,
        memberCount = this.memberCount,
        channels = this.channels,
        maxPresences = this.maxPresences,
        maxMembers = this.maxMembers,
        vanityUrlCode = this.vanityUrlCode,
        description = this.description,
        banner = this.banner,
        premiumTier = this.premiumTier,
        premiumSubscriptionCount = this.premiumSubscriptionCount,
        preferredLocale = this.preferredLocale,
        publicUpdatesChannelId = this.publicUpdatesChannelId,
        maxVideoChannelUsers = this.maxVideoChannelUsers,
        approximatePresenceCount = this.approximatePresenceCount,
        approximateMemberCount = this.approximateMemberCount,
        welcomeScreen = this.welcomeScreen.map { WelcomeScreenData.from(it) },
        nsfwLevel = this.nsfwLevel,
        threads = this.threads,
    )
}

@Serializable
data class TotalStats(val total: Stats, @SerialName("shard_stats") val shardStats: Map<Int, Stats>)
@Serializable
data class Stats(val guilds: Int, val unavailable: Int)