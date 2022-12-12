package com.yama.marshal.service

import com.yama.marshal.repository.CompanyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object MarshalNotificationService : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val companyRepository = CompanyRepository()

    fun start() = this.launch(Dispatchers.Default) {
        while (true) {
            delay(2 * 1000L)

            companyRepository.loadCarts()
            companyRepository.loadCartsRound()
        }
    }
}