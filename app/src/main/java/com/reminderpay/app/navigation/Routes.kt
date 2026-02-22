package com.reminderpay.app.navigation

/**
 * All navigation routes used in the app.
 * Arguments are embedded in the path using curly-brace notation.
 */
object Routes {
    const val HOME   = "home"
    const val ADD    = "add"
    const val DETAIL = "detail/{reminderId}"
    const val EDIT   = "edit/{reminderId}"
    const val HISTORY = "history"

    fun detailRoute(id: Int) = "detail/$id"
    fun editRoute(id: Int)   = "edit/$id"
}
