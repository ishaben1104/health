package com.example.health

import android.widget.Switch

data class Patient(
    val patientId: String?=null,
    val firstname: String?=null,
    val lastname: String?=null,
    val phone: String?=null,
    val email: String?=null,
    val dob: String?=null,
    val username: String?=null,
    val isVIP: Boolean,
    val paidAmount: Int?=null,
    val paymentStatus: String?=null,
)
