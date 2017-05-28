package corouties

import kotlinx.coroutines.experimental.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val c = AtomicInteger()

    println("start")

    val deferred = (1..1_000_000).map { n ->
        async(CommonPool) {
            delay(1000)
            c.addAndGet(1)
            n
        }
    }

    runBlocking {
        val sum = deferred.sumBy { it.await() }
        println("Sum = $sum")
    }


    println("c = ${c.get()}")
}
