package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.Donation
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations ORDER BY timestamp DESC")
    fun getAllDonations(): Flow<List<Donation>>

    @Query("SELECT * FROM donations WHERE campaignId = :campaignId ORDER BY timestamp DESC")
    fun getDonationsForCampaign(campaignId: String): Flow<List<Donation>>

    @Query("SELECT SUM(amount) FROM donations")
    fun getTotalDonatedAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM donations")
    fun getTotalDonationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(donations: List<Donation>)

    @Query("SELECT COUNT(*) FROM donations")
    suspend fun getDonationsRecordCount(): Int
}
