package com.yama.marshal.data.model

sealed class CartMessageModel(
    open val cartID: Int,
    open val message: String
) {
    data class Emergency(
        override val cartID: Int,
        override val message: String
    ): CartMessageModel(cartID, message)

    data class Issue(
        override val cartID: Int,
        override val message: String
    ): CartMessageModel(cartID, message)

    data class Custom(
        override val cartID: Int,
        override val message: String
    ): CartMessageModel(cartID, message)
}
