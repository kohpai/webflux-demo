package io.kohpai.webfluxdemo

data class Joke(
    val category: Category,
    val joke: String,
    val overlong: Boolean   // longer than 100 characters
) {
    constructor(joke: String) : this(
        Category.of(joke),
        joke,
        joke.length > 100
    )
}