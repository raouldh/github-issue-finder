package nl.rdh.github.extensions

import org.springframework.http.ResponseEntity

fun <T> ResponseEntity<List<T>>.bodyAsList() = this.body ?: emptyList()