package com.yama.marshal.repository.unit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class YamaRepository : CoroutineScope  {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
}