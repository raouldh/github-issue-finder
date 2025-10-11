package nl.rdh.github.extensions

fun <T, U> Collection<T>.parallelFlatMap(block: (T) -> List<U>): List<U> = this
    .parallelStream()
    .flatMap { block(it).stream() }
    .toList()