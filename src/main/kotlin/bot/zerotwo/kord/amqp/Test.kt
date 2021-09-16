package bot.zerotwo.kord.amqp

import bot.zerotwo.kord.core.AmqpKord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        /*
        val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val event = jsonParser.decodeFromString(Event.Companion, "{\"t\":\"USER_UPDATE\",\"d\":{\"id\":\"123\",\"username\":\"Tester\",\"discriminator\":\"1234\"},\"op\":0}") ?: return
        println(event)*/

        GlobalScope.launch {
            val kord = AmqpKord(
                System.getenv("TOKEN"),
                System.getenv("TOTAL_SHARDS").toInt(),
                System.getenv("AMQP_URI"),
                listOf("MESSAGE_CREATE", "INTERACTION_CREATE")
            )

            kord.on<MessageCreateEvent> {

                println(currentCoroutineContext())

                val guild = this.getGuild()?.name ?: "DM"
                val channel = this.message.getChannelOrNull()?.data?.name?.value ?: "Err"
                val user = this.message.author?.username ?: "Err"

                println("[$guild / $channel] $user: ${message.content}")
            }
        }.asCompletableFuture().get()

    }
}