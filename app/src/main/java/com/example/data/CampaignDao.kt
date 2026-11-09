package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns WHERE status = 'APPROVED' ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllApprovedCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun getCampaignById(id: String): Flow<Campaign?>

    @Query("SELECT * FROM campaigns WHERE isFeatured = 1 AND status = 'APPROVED' LIMIT 5")
    fun getFeaturedCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE status = 'APPROVED' AND categoryId = :categoryId ORDER BY createdAt DESC")
    fun getCampaignsByCategory(categoryId: String): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaignsForAdmin(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE organizerPhone = :phone OR organizerName = :name ORDER BY createdAt DESC")
    fun getUserCampaigns(phone: String, name: String): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(campaigns: List<Campaign>)

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Query("UPDATE campaigns SET status = :status WHERE id = :id")
    suspend fun updateCampaignStatus(id: String, status: String)

    @Query("UPDATE campaigns SET currentAmount = currentAmount + :donationAmount, donorsCount = donorsCount + 1 WHERE id = :id")
    suspend fun recordDonation(id: String, donationAmount: Double)

    @Query("UPDATE campaigns SET updatesRaw = CASE WHEN updatesRaw = '' THEN :newUpdate ELSE :newUpdate || '###' || updatesRaw END WHERE id = :id")
    suspend fun appendCampaignUpdate(id: String, newUpdate: String)

    @Query("SELECT COUNT(*) FROM campaigns")
    suspend fun getCampaignCount(): Int
}
