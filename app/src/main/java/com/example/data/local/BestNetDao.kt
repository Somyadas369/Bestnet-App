package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommunityNotice
import com.example.data.model.Complaint
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import kotlinx.coroutines.flow.Flow

@Dao
interface BestNetDao {

  // Residents
  @Query("SELECT * FROM residents ORDER BY id ASC")
  fun getAllResidents(): Flow<List<Resident>>

  @Query("SELECT * FROM residents WHERE isCurrent = 1 LIMIT 1")
  fun getCurrentResident(): Flow<Resident?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResidents(residents: List<Resident>)

  @Query("UPDATE residents SET isCurrent = CASE WHEN id = :residentId THEN 1 ELSE 0 END")
  suspend fun switchCurrentResident(residentId: Long)

  /**
   * Server sync replaces these tables wholesale rather than upserting, because
   * Room ids are autoGenerate Longs and the server's are UUID strings — there
   * is no stable key to match rows on. Replacing also means a home the resident
   * no longer belongs to actually disappears.
   */
  @Query("DELETE FROM residents")
  suspend fun clearResidents()

  @Query("DELETE FROM notices")
  suspend fun clearNotices()

  @Query("DELETE FROM visitors")
  suspend fun clearVisitors()

  @Query("DELETE FROM complaints")
  suspend fun clearComplaints()

  /**
   * One-shot read of the selected home, for code that needs the unit label
   * outside a Flow collector (visit sync, which runs inside a suspend call).
   */
  @Query("SELECT * FROM residents WHERE isCurrent = 1 LIMIT 1")
  suspend fun currentResidentOnce(): Resident?

  // Complaints
  @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
  fun getAllComplaints(): Flow<List<Complaint>>

  @Query("SELECT * FROM complaints WHERE status = :status ORDER BY createdAt DESC")
  fun getComplaintsByStatus(status: String): Flow<List<Complaint>>

  @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
  fun getComplaintById(id: Long): Flow<Complaint?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertComplaint(complaint: Complaint): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertComplaints(complaints: List<Complaint>)

  @Update
  suspend fun updateComplaint(complaint: Complaint)

  @Query("UPDATE complaints SET status = :status, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateComplaintStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

  @Query("DELETE FROM complaints WHERE id = :id")
  suspend fun deleteComplaintById(id: Long)

  // Visitors
  @Query("SELECT * FROM visitors ORDER BY id DESC")
  fun getAllVisitors(): Flow<List<Visitor>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVisitor(visitor: Visitor): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVisitors(visitors: List<Visitor>)

  // Notices
  @Query("SELECT * FROM notices ORDER BY id ASC")
  fun getAllNotices(): Flow<List<Notice>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotices(notices: List<Notice>)

  @Query("UPDATE notices SET isRead = 1 WHERE id = :id")
  suspend fun markNoticeAsRead(id: Long)

  @Query("UPDATE notices SET isRead = 1")
  suspend fun markAllNoticesAsRead()

  // Community Notices (Chronological order)
  @Query("SELECT * FROM community_notices ORDER BY timestamp DESC")
  fun getAllCommunityNotices(): Flow<List<CommunityNotice>>

  @Query("SELECT * FROM community_notices WHERE category = :category ORDER BY timestamp DESC")
  fun getCommunityNoticesByCategory(category: String): Flow<List<CommunityNotice>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommunityNotice(notice: CommunityNotice): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommunityNotices(notices: List<CommunityNotice>)

  @Query("DELETE FROM community_notices WHERE id = :id")
  suspend fun deleteCommunityNotice(id: Long)
}
