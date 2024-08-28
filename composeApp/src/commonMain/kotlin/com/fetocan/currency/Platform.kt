package com.fetocan.currency

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform