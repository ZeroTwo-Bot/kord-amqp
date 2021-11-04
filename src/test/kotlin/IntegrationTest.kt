import bot.zerotwo.kord.core.AmqpKord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 5, unit = TimeUnit.MINUTES)
class IntegrationTest {

    @Test
    fun test() {
        GlobalScope.launch {
            val kord = AmqpKord(
                System.getenv("TOKEN"),
                System.getenv("TOTAL_SHARDS").toInt(),
                System.getenv("AMQP_URI"),
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