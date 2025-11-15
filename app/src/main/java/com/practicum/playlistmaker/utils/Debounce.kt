package com.practicum.playlistmaker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun <T> debounce(
    delayMillis: Long,
    scope: CoroutineScope,
    action: (T) -> Unit
): (T) -> Unit {
    var job: Job? = null

    return { param: T ->
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            action(param)
        }
    }
}
