package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val titleAm: String = "",
    val message: String,
    val messageAm: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "donation", // donation, campaign, verification, system
    val targetCampaignId: String? = null
)
