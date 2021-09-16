package bot.zerotwo.kord.core

import dev.kord.common.entity.Snowflake
import kotlin.coroutines.CoroutineContext

object ContextKeys {
    val REQUEST_META_KEY = RequestMetaKey()
    val EVENT_CACHE = EventCacheKey()
}

class RequestMetaKey : CoroutineContext.Key<RequestMeta>

class RequestMeta(override val key: CoroutineContext.Key<*>, val guildId: Snowflake? = null, val shardId: Int? = null, val botId: Snowflake?) : CoroutineContext.Element

class EventCacheKey : CoroutineContext.Key<EventCache>

class EventCache(override val key: CoroutineContext.Key<*>) : CoroutineContext.Element {

    private val cache = mutableMapOf<String, Any?>();

    fun push(key: String, element: Any?) {
        cache[key] = element
    }

    fun <T> get(key: String): T? {
        return cache[key] as? T
    }

    fun drop(key: String) {
        cache.remove(key)
    }

    fun drop() {
        cache.clear()
    }
}