package com.example.urbanfit

import java.util.*

data class BookingGYM(
    val hour: String,
    val date: Calendar,
    val className: String,
    val associatedGym: String
)
