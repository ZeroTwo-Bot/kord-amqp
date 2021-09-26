package bot.zerotwo.kord.core.event

enum class EventBinding {

    // State
    Ready,
    Resumed,

    // Channel
    ChannelCreate,
    ChannelUpdate,
    ChannelDelete,
    ThreadCreate,
    ThreadUpdate,
    ThreadDelete,
    ThreadListSync,
    ThreadMemberUpdate,
    ThreadMembersUpdate,
    ChannelPinsUpdate,

    //Guild
    GuildCreate,
    GuildUpdate,
    GuildDelete,
    GuildBanAdd,
    GuildBanRemove,
    GuildEmojisUpdate,
    GuildStickersUpdate,
    GuildIntegrationsUpdate,
    GuildMemberAdd,
    GuildMemberRemove,
    GuildMemberUpdate,
    GuildMembersChunk,
    GuildRoleCreate,
    GuildRoleUpdate,
    GuildRoleDelete,

    // Integration
    IntegrationCreate,
    IntegrationUpdate,
    IntegrationDelete,

    // Invite
    InviteCreate,
    InviteDelete,

    // Message
    MessageCreate,
    MessageUpdate,
    MessageDelete,
    MessageDeleteBulk,

    // Reaction
    MessageReactionAdd,
    MessageReactionRemove,
    MessageReactionRemoveAll,
    MessageReactionRemoveEmoji,

    // Misc
    PresenceUpdate,
    TypingStart,
    UserUpdate,

    // Voice
    VoiceStateUpdate,
    VoiceServerUpdate,

    // Webhook
    WebhooksUpdate,

    // Commands
    ApplicationCommandCreate,
    ApplicationCommandUpdate,
    ApplicationCommandDelete,

    // Interactions
    InteractionCreate,

    // Stage Instances
    StageInstanceCreate,
    StageInstanceUpdate,
    StageInstanceDelete,

    // Unused
    GuildJoinRequestDelete,
    ApplicationCommandPermissionsUpdate,
    GiftCodeUpdate,
    GuildScheduledDeleteEvent,
}