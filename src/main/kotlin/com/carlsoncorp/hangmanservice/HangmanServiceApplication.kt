package com.carlsoncorp.hangmanservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HangmanServiceApplication

fun main(args: Array<String>) {
	runApplication<HangmanServiceApplication>(*args)
}
