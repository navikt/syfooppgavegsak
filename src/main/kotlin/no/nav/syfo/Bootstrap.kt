package no.nav.syfo

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import no.nav.syfo.api.registerNaisApi
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

private val log = LoggerFactory.getLogger("nav.syfo.gsak")

fun main(args: Array<String>) {
    val env = Environment()
    val applicationState = ApplicationState()

    val applicationServer = embeddedServer(Netty, env.applicationPort) {
        initRouting(applicationState)
    }.start(wait = false)

    try {
        val listeners = (1..env.applicationThreads).map {
            launch {
                val consumerProperties = readConsumerConfig(env, valueDeserializer = StringDeserializer::class)
                val consumer = KafkaConsumer<String, String>(consumerProperties)
                consumer.subscribe(listOf(env.kafkaSM2013OppgaveGsakTopic))

                blockingApplicationLogic(applicationState, consumer)
            }
        }.toList()

        applicationState.initialized = true

        runBlocking {
            Runtime.getRuntime().addShutdownHook(Thread {
                applicationServer.stop(10, 10, TimeUnit.SECONDS)
            })
            listeners.forEach { it.join() }
        }
    } finally {
        applicationState.running = false
    }
}

suspend fun blockingApplicationLogic(applicationState: ApplicationState, consumer: KafkaConsumer<String, String>) {
    while (!applicationState.running) {
        consumer.poll(Duration.ofMillis(0)).forEach {
            println(it.value())
        }
        delay(100)
    }
}

fun Application.initRouting(applicationState: ApplicationState) {
    routing {
        registerNaisApi(
                readynessCheck = {
                    applicationState.initialized
                },
                livenessCheck = {
                    applicationState.running
                }
        )
    }
}
