package br.com.zup.felipe.gadelha

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.felipe.gadelha")
		.start()
}

