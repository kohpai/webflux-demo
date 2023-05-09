package io.kohpai.webfluxdemo

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.util.logging.Logger

private val JOKES = arrayOf(
    "Chuck Norris hat als Kind Sandburgen gebaut. Wir kennen sie heute als Pyramiden.",
    "Was ist der Unterschied zwischen Batman und Microsoft? - Batman hat den Pinguin besiegt.",
    "Chuck Norris kennt die letzte Ziffer von Pi.",
    "Was ist flach und eckig? - Ein Minecraft-Witz.",
    "Chuck Norris ist Darth Vaders Vater.",
    "Jesus, ein Priester und Chuck Norris sitzen in einem Boot auf dem Ozean. Jesus: \"Ich kann über Wasser gehen.\" - und er geht über Wasser. Chuck Norris: \"Ich auch.\" - und er geht hinter Jesus auf dem Wasser her. Der Priester betet zu Gott: \"Herr, steh mir bei und mach, dass ich auch über Wasser laufen kann.\" Der Priester steigt aus dem Boot - und geht unter. Daraufhin Jesus zu Chuck Norris: \"Meinst du, wir hätten ihm sagen sollen, wo die Steine sind?\" Chuck Norris: \"Welche Steine?\""
)

@RestController
@RequestMapping("/resources/jokes")
class JokeResource {
    private val logger = Logger.getLogger(javaClass.name)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('READ')")
    fun streamAll() =
        Flux.fromArray(JOKES).map { Joke(it) }
            .delayElements(Duration.ofSeconds(1))
            .doOnSubscribe { logger.info("SUBSCRIBED") }
            .doOnCancel { logger.info("CANCELLED") }
            .doOnTerminate { logger.info("TERMINATED") }
            .doOnNext { logger.info("NEXT SENT >> joke=$it") }
            .doOnError { logger.info("ERROR SENT") }
            .doOnComplete { logger.info("COMPLETE SENT") }

    private val sink = Sinks.many().replay().latest<Joke>()

    @PostConstruct
    fun init() {
        JOKES.forEach { sink.tryEmitNext(Joke(it)) }
    }

    @PreDestroy
    fun done() {
        sink.tryEmitComplete()
    }

    @PostMapping(consumes = [MediaType.TEXT_PLAIN_VALUE])
    @PreAuthorize("hasRole('WRITE')")
    fun create(@RequestBody joke: String): Mono<Void> {
        sink.tryEmitNext(Joke(joke)).orThrow()
        return Mono.empty()
    }

    @PreAuthorize("permitAll()")
    @GetMapping(path = ["/live"], produces = [MediaType.APPLICATION_NDJSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE])
    fun steamLive(): Flux<Joke> {
        logger.info("Sending live data")

        return sink.asFlux()
            .delayElements(Duration.ofSeconds(1))
            .doOnSubscribe { logger.info("SUBSCRIBED") }
            .doOnCancel { logger.info("CANCELLED") }
            .doOnTerminate { logger.info("TERMINATED") }
            .doOnNext { logger.info("NEXT SENT >> joke=$it") }
            .doOnError { logger.info("ERROR SENT") }
            .doOnComplete { logger.info("COMPLETE SENT") }
    }
}