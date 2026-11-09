package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey
    val id: String,
    val campaignId: String,
    val campaignTitle: String,
    val donorName: String,
    val donorPhone: String,
    val donorEmail: String = "",
    val isAnonymous: Boolean = false,
    val amount: Double,
    val platformTip: Double = 0.0,
    val feeAmount: Double = 0.0,
    val totalPaid: Double = amount + platformTip + feeAmount,
    val paymentMethod: String = "Telebirr", // Telebirr, CBE Birr, Awash Bank, Card
    val transactionReference: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED" // COMPLETED, PENDING, FAILED
)
