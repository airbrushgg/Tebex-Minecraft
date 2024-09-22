package io.tebex.plugin.util

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object Multithreading {
    private val counter = AtomicInteger(0)

    private val RUNNABLE_POOL: ScheduledExecutorService = Executors.newScheduledThreadPool(
        10
    ) { r: Runnable? ->
        Thread(
            r,
            "Tebex Thread " + counter.incrementAndGet()
        )
    }

    var POOL: ThreadPoolExecutor = ThreadPoolExecutor(
        10, 30,
        0L, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ) { r: Runnable? ->
        Thread(
            r,
            String.format("Thread %s", counter.incrementAndGet())
        )
    }

    fun schedule(r: Runnable?, initialDelay: Long, delay: Long, unit: TimeUnit?): ScheduledFuture<*> {
        return RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit)
    }

    fun schedule(r: Runnable?, delay: Long, unit: TimeUnit?): ScheduledFuture<*> {
        return RUNNABLE_POOL.schedule(r, delay, unit)
    }

    fun delayedExecutor(delay: Long, unit: TimeUnit?): Executor {
        return Executor { task: Runnable? ->
            schedule(
                task,
                delay,
                unit
            )
        }
    }

    fun runAsync(runnable: Runnable?) {
        POOL.execute(runnable)
    }

    fun submit(runnable: Runnable?): Future<*> {
        return POOL.submit(runnable)
    }

    fun executeAsync(runnable: Runnable?) {
        runAsync(runnable)
    }

    fun executeAsyncLater(runnable: Runnable?, time: Long, unit: TimeUnit?) {
        schedule(runnable, time, unit)
    }

    fun executeAsync(runnable: Runnable?, initialDelay: Long, time: Long, unit: TimeUnit?) {
        schedule(runnable, initialDelay, time, unit)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun executeBlocking(runnable: Runnable?) {
        val future = submit(runnable)
        future.get() // This will block until the task is completed
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun executeBlockingLater(runnable: Runnable?, time: Long, unit: TimeUnit?) {
        val future: Future<*> = schedule(runnable, time, unit)
        future.get() // This will block until the task is completed
    }

    fun shutdown() {
        POOL.shutdown()
        RUNNABLE_POOL.shutdown()
    }
}