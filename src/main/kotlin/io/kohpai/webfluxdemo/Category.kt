package io.kohpai.webfluxdemo

enum class Category {
    CHUCK_NORRIS,
    OTHER;

    companion object {
        fun of(joke: String) = if (joke.contains("Chuck Norris", true)) CHUCK_NORRIS else OTHER
    }
}