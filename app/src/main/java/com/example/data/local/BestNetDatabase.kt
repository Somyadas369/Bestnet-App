package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Complaint
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [Resident::class, Complaint::class, Visitor::class, Notice::class],
  version = 1,
  exportSchema = false
)
abstract class BestNetDatabase : RoomDatabase() {
  abstract fun bestNetDao(): BestNetDao

  companion object {
    @Volatile
    private var INSTANCE: BestNetDatabase? = null

    fun getDatabase(context: Context): BestNetDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BestNetDatabase::class.java,
          "bestnet_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            populateDatabase(database.bestNetDao())
          }
        }
      }

      suspend fun populateDatabase(dao: BestNetDao) {
        dao.insertResidents(
          listOf(
            Resident(
              name = "Rahul Sharma",
              phone = "+91 98765 43210",
              unit = "A-1201",
              communityName = "Sunrise Apartments",
              isCurrent = true
            ),
            Resident(
              name = "Rahul Sharma",
              phone = "+91 98765 43210",
              unit = "B-404",
              communityName = "Green Valley Residency",
              isCurrent = false
            )
          )
        )

        dao.insertVisitors(
          listOf(
            Visitor(
              name = "Rakesh Mehta",
              type = "Delivery",
              unit = "A-1201",
              timestampText = "04 Sep 2026, 10:25 AM",
              status = "Approved",
              isPreApproved = false
            ),
            Visitor(
              name = "Swiggy Delivery",
              type = "Delivery",
              unit = "A-1201",
              timestampText = "03 Sep 2026, 08:12 PM",
              status = "Approved",
              isPreApproved = false
            ),
            Visitor(
              name = "Amit Verma",
              type = "Guest",
              unit = "A-1201",
              timestampText = "02 Sep 2026, 06:40 PM",
              status = "Approved",
              isPreApproved = true,
              passCode = "948210"
            ),
            Visitor(
              name = "Technician",
              type = "Service",
              unit = "A-1201",
              timestampText = "01 Sep 2026, 11:20 AM",
              status = "Approved",
              isPreApproved = false
            )
          )
        )

        dao.insertNotices(
          listOf(
            Notice(
              title = "Water Supply Update",
              body = "Water supply will be shut down tomorrow from 10 AM to 2 PM.",
              category = "Services",
              timestampText = "2 hours ago",
              isRead = false,
              iconType = "water"
            ),
            Notice(
              title = "Maintenance Notice",
              body = "Lift maintenance scheduled on 5th Sep 2026.",
              category = "Announcements",
              timestampText = "5 hours ago",
              isRead = false,
              iconType = "maintenance"
            ),
            Notice(
              title = "Community Event",
              body = "Independence Day celebration on 15th Aug at 6 PM.",
              category = "Announcements",
              timestampText = "1 day ago",
              isRead = false,
              iconType = "event"
            ),
            Notice(
              title = "Your Complaint Updated",
              body = "Your complaint #C2026-145 has been resolved.",
              category = "Services",
              timestampText = "1 day ago",
              isRead = true,
              iconType = "complaint"
            ),
            Notice(
              title = "New Visitor Entry",
              body = "Rakesh Mehta has visited your flat.",
              category = "Community",
              timestampText = "2 days ago",
              isRead = true,
              iconType = "visitor"
            )
          )
        )
      }
    }
  }
}
