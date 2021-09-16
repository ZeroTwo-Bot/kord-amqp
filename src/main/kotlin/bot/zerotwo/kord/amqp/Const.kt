package bot.zerotwo.kord.amqp

import kotlinx.serialization.json.Json

object Const {
    val JSON = Json {
        ignoreUnknownKeys = true
    }
}