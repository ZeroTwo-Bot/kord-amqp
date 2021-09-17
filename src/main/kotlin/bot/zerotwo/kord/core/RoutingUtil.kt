package bot.zerotwo.kord.core

import dev.kord.common.entity.Snowflake

internal object RoutingTable {
    val shards: MutableMap<Snowflake, Int> = mutableMapOf()
    var defaultAppId: Snowflake = Snowflake.min;
}

fun setDefaultAppId(appId: Snowflake) {
    RoutingTable.defaultAppId = appId;
}

fun getDefaultAppId(): Snowflake {
    return RoutingTable.defaultAppId
}

fun setTotalShards(appId: Snowflake = RoutingTable.defaultAppId, shards: Int) {
    RoutingTable.shards[appId] = shards
}

fun getShardByGuildId(appId: Snowflake = RoutingTable.defaultAppId, guildId: Snowflake): Int {
    val totalShards = RoutingTable.shards[appId] ?: throw IllegalStateException("Application ID is not registered in Routing Table")
    return ((guildId.value shr 22) % totalShards.toULong()).toInt()
}