package com.phani.helloworld.models

data class User(
    val uId: String? = null,
    val name: String? = null,
    val profile: String? = null,
    val city: String? = null
) {
    constructor(): this("", "", "", "Unknown")
}
