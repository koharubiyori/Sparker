package koharubiyori.sparker.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

private val coroutine = CoroutineScope(Dispatchers.Default)

class IntervalTask(
  private val intervalTime: Duration,
  autoStart: Boolean = true,
  private val task: suspend (IntervalTask) -> Unit,
) {
  private var keepRunning = false

  init {
    if (autoStart) start()
  }

  private fun loop() = coroutine.launch {
    while (keepRunning) {
      task(this@IntervalTask)
      delay(intervalTime)
    }
  }

  fun start() {
    keepRunning = true
    loop()
  }

  fun stop() {
    keepRunning = false
  }
}