package com.example.urbanfit.ui.home

import java.util.*

data class BookingGYM(
    val id: String,
    val hour: String,
    val date: Calendar,
    val className: String,
    val associatedGym: String
)
