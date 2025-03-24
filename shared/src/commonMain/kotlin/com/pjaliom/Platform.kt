package com.pjaliom

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform