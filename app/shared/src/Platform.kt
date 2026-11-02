package co.ke.xently.expense.tracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
