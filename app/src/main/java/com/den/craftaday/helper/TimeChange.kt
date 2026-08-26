package com.den.craftaday.helper

sealed class TimeChange {
    data class Years(val value: Long) : TimeChange()
    data class Months(val value: Long) : TimeChange()
    data class Days(val value: Long) : TimeChange()
    data class Hours(val value: Long) : TimeChange()
    data class Minutes(val value: Long) : TimeChange()

    data class SpecificDate(val value: String) : TimeChange()
    data class SpecificWeekDay(val value: String) : TimeChange()
    object JustNow : TimeChange()
}