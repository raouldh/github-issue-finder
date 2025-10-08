package nl.rdh.github

import org.springframework.http.ResponseEntity

fun <T> ResponseEntity<List<T>>.bodyAsList() = this.body ?: emptyList<T>()