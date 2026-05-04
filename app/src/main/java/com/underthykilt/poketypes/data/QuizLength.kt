package com.underthykilt.poketypes.data

enum class QuizLength(val label: String, val count: Int?) {
    FIVE("5", 5),
    TEN("10", 10),
    TWENTY("20", 20),
    ENDLESS("∞", null)
}
