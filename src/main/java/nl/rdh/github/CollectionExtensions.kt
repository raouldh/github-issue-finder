package nl.rdh.github

fun <T, U> Collection<T>.mapParallel(block: (T) -> U): List<U> = this
    .parallelStream()
    .map { block(it) }
    .toList()

fun <T, U> Collection<T>.flatMapParallel(block: (T) -> List<U>): List<U> = this
    .parallelStream()
    .flatMap { block(it).stream() }
    .toList()