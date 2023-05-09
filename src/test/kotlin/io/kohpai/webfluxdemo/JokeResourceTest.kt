package io.kohpai.webfluxdemo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.test.StepVerifier
import java.util.logging.Logger

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JokeResourceTest(@Autowired private val webTestClient: WebTestClient) {
    private val logger = Logger.getLogger(javaClass.name)

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        println(
            String.format("%n===== ${testInfo.displayName} =====%n")
        )
    }

    @Test
    fun testStreamAll() {
        val flux = webTestClient
            .get()
            .uri("/resources/jokes")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .returnResult(Joke::class.java)
            .responseBody
            .doOnNext { logger.info("NEXT RECEIVED >> joke=$it") }
            .doOnError { logger.info("ERROR RECEIVED") }
            .doOnComplete { logger.info("COMPLETE RECEIVED") }

        StepVerifier
            .create(flux)
            .expectSubscription()
            .expectNextCount(6)
            .verifyComplete()
    }

    @Test
    fun testStreamLive() {
        val flux = webTestClient
            .get()
            .uri("/resources/jokes/live")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .returnResult(Joke::class.java)
            .responseBody
            .doOnNext { logger.info("NEXT RECEIVED >> joke=$it") }
            .doOnError { logger.info("ERROR RECEIVED") }
            .doOnComplete { logger.info("COMPLETE RECEIVED") }

        val jokeToSend = "Was ist ein Keks unter einem Baum? - Ein schattiges Plätzchen"

        val mono = webTestClient
            .post()
            .uri("/resources/jokes")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue(jokeToSend)
            .exchange()
            .returnResult(Void::class.java)
            .responseBody
            .next()

        StepVerifier
            .create(flux)
            .expectSubscription()
            .expectNextCount(1)
            .then {
                mono.block()
            }
            .expectNextMatches { jokeToSend == it.joke }
            .thenCancel()
            .verify()
    }
}