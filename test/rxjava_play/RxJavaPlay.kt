package rxjava_play

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.junit.Before
import org.junit.Test
import rx.Observable.*
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.ThreadFactory


class RxJavaPlay {
    companion object {
        val NUM_OF_THREADS = 5
    }

    val schedulerA = Schedulers.from(newFixedThreadPool(NUM_OF_THREADS, threadFactory("Sched-A-%d")))
    val schedulerB = Schedulers.from(newFixedThreadPool(NUM_OF_THREADS, threadFactory("Sched-B-%d")))
    val schedulerC = Schedulers.from(newFixedThreadPool(NUM_OF_THREADS, threadFactory("Sched-C-%d")))

    private fun threadFactory(pattern: String): ThreadFactory {
        return ThreadFactoryBuilder().setNameFormat(pattern).build()
    }

    @Before
    fun setUp() {
        startTime = Date().time
    }

    @Test
    fun rxjava_defer() {
        class Foo(var value: String) {
            fun observable() = defer { just(value) }
        }

        val foo = Foo("a")

        val o = foo.observable()

        foo.value = "b"

        val ts = TestSubscriber<String>()

        o.subscribe(ts)

        ts.assertCompleted()
        ts.assertValue("b")
    }

    @Test
    fun rxjava_zip_threaded() {
        val random = Random()

        val oi = from(1..10)
                .concatMap {
                    just(it)
                            .doOnNext { Thread.sleep(random.nextInt(5000).toLong()); printWithThreadInfo(it) }
                            .subscribeOn(schedulerA)
                }

        val oc = from('a'..'z')
                .take(10)
                .concatMap {
                    just(it)
                            .doOnNext { Thread.sleep(random.nextInt(5000).toLong()); printWithThreadInfo(it) }
                            .subscribeOn(schedulerB)
                }


        val ts = TestSubscriber<Pair<Int,Char>>()

        zip(oi, oc) { i: Int, c: Char -> i to c }
                .observeOn(schedulerC)
                .doOnNext { printWithThreadInfo(it) }
                .subscribe(ts)

        ts.awaitTerminalEvent()
    }


    var startTime = Date().time

    fun <T> printWithThreadInfo(i: T) {
        println("${Date().time - startTime}: value $i on thread ${Thread.currentThread().name}")
    }
}
