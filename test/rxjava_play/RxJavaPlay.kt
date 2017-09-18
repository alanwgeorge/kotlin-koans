package rxjava_play

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.junit.Before
import org.junit.Test
import rx.Observable.*
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit


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

    @Test
    fun rxjava_session_timeout() {
        val signOnSuccess = "signOnSuccess"
        val timeoutTimeUnit = TimeUnit.MILLISECONDS
        val warningTime = 2500L
        val sessionTimeout = 3000L

        val signOnEvents = PublishSubject.create<String>()
        val networkEvents = PublishSubject.create<Int>()

        val eventGeneratorObs = create<Unit> { sub ->
            Thread.sleep(1000)
            networkEvents.onNext(1)
            signOnEvents.onNext("signOnInit")
            Thread.sleep(250)
            signOnEvents.onNext(signOnSuccess)
            Thread.sleep(500)
            signOnEvents.onNext("someOtherSignOnEvent")
            networkEvents.onNext(2)
            Thread.sleep(500)
            networkEvents.onNext(3)
            networkEvents.onNext(4)
            Thread.sleep(1000)
            networkEvents.onNext(5)
        }

        val sessionStartObs = signOnEvents
                .filter { it?.equals(signOnSuccess) }
                .map { SessionStart(System.currentTimeMillis()) }
                .share()

        val sessionTimeoutWarningObs = sessionStartObs
                .switchMap { start ->
                    networkEvents
                            .doOnNext { printWithThreadInfo("in session network event $it: session length ${System.currentTimeMillis() - start.time}") }
                            .debounce(warningTime, timeoutTimeUnit, schedulerB)
                            .map { SessionTimeoutWarning(System.currentTimeMillis() - start.time) }
                }
                .share()

        val sessionTimeoutObs = sessionStartObs
                .switchMap { start ->
                    networkEvents
                            .doOnNext { printWithThreadInfo("in session network event $it: session length ${System.currentTimeMillis() - start.time}") }
                            .debounce(sessionTimeout, timeoutTimeUnit, schedulerC)
                            .map { SessionTimeout(System.currentTimeMillis() - start.time) }
                }

        val ts1 = TestSubscriber<SessionTimeout>()

        eventGeneratorObs
                .subscribeOn(schedulerA)
                .subscribe()

        sessionTimeoutWarningObs
                .doOnNext { printWithThreadInfo("sessionTimeoutWarning: $it") }
                .subscribe()

        sessionTimeoutWarningObs
                .first()
                .doOnNext {
                    printWithThreadInfo("extending session: $it")
                    networkEvents.onNext(6)
                }
                .subscribe()

        sessionTimeoutObs
                .first()
                .doOnNext { printWithThreadInfo("sessionTimeout: $it") }
                .subscribe(ts1)

        ts1.awaitTerminalEvent()
    }

    var startTime = Date().time

    fun <T> printWithThreadInfo(i: T) {
        println("${Date().time - startTime}: value $i on thread ${Thread.currentThread().name}")
    }
}

data class SessionStart(val time: Long)
data class SessionTimeout(val sessionLength: Long)
data class SessionTimeoutWarning(val sessionLength: Long)