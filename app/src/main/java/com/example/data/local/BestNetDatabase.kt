package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CommunityNotice
import com.example.data.model.Complaint
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [Resident::class, Complaint::class, Visitor::class, Notice::class, CommunityNotice::class],
  version = 3,
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

        dao.insertComplaints(
          listOf(
            Complaint(
              ticketNumber = "#CMP-2026-302",
              title = "Main Kitchen Switchboard Sparking",
              category = "Electrician",
              description = "The 16A microwave socket has loose wiring and emits sparks when heavy appliances are turned on.",
              unit = "A-1201",
              priority = "High",
              status = "Pending",
              assignedTo = "Awaiting Electrician Assignment",
              createdAt = System.currentTimeMillis() - (2 * 3600 * 1000)
            ),
            Complaint(
              ticketNumber = "#CMP-2026-289",
              title = "Master Bathroom Flush Cistern Leakage",
              category = "Plumber",
              description = "Water is continuously trickling into the commode bowl, creating water wastage.",
              unit = "A-1201",
              priority = "Medium",
              status = "In Progress",
              assignedTo = "Technician Ramesh K. (Plumbing Team)",
              createdAt = System.currentTimeMillis() - (18 * 3600 * 1000)
            ),
            Complaint(
              ticketNumber = "#CMP-2026-145",
              title = "Fiber Broadband High Ping & Packet Drop",
              category = "Internet Issue",
              description = "Experiencing intermittent disconnections during Zoom office calls between 7 PM and 9 PM.",
              unit = "A-1201",
              priority = "Urgent",
              status = "Resolved",
              assignedTo = "BestNet Fiber NOC Team",
              resolutionNotes = "Splitter re-spliced on 12th floor distribution box. Signal optical power verified at -18.5 dBm.",
              createdAt = System.currentTimeMillis() - (48 * 3600 * 1000),
              updatedAt = System.currentTimeMillis() - (12 * 3600 * 1000)
            ),
            Complaint(
              ticketNumber = "#CMP-2026-118",
              title = "Balcony Sliding Glass Door Roller Stuck",
              category = "General Maintenance",
              description = "The sliding mesh and glass panel jams halfway along the lower runner.",
              unit = "A-1201",
              priority = "Low",
              status = "Resolved",
              assignedTo = "Estate Carpentry Dept",
              resolutionNotes = "Debris cleared from bottom rail; nylon rollers lubricated and re-aligned.",
              createdAt = System.currentTimeMillis() - (72 * 3600 * 1000),
              updatedAt = System.currentTimeMillis() - (36 * 3600 * 1000)
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

        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        dao.insertCommunityNotices(
          listOf(
            CommunityNotice(
              title = "Urgent: Water Tank Cleaning & Supply Halt",
              description = "Overhead water storage tanks in Towers A and B will undergo deep microbial cleaning and UV purification. Water supply will be completely shut down from 10:00 AM to 02:00 PM tomorrow. Residents are requested to store adequate water for morning usage.",
              timestamp = now - (2 * hour),
              category = "Maintenance",
              priority = "Urgent",
              author = "Estate Facility Operations Desk",
              isPinned = true
            ),
            CommunityNotice(
              title = "Annual Resident Welfare Association General Body Meeting",
              description = "The Annual General Body Meeting (AGM) of Sunrise Apartments RWA is scheduled for this Sunday at 10:30 AM in the Clubhouse Banquet Hall. Key agenda items include FY2026-27 balance sheet approval, security vendor renewal, EV charging bay installation, and playground upgrades.",
              timestamp = now - (6 * hour),
              category = "Association",
              priority = "Important",
              author = "Secretary, Sunrise RWA Executive Committee",
              isPinned = true
            ),
            CommunityNotice(
              title = "Passenger Lift Modernization Schedule (Tower A)",
              description = "Lift No. 2 in Tower A is undergoing annual safety inspection and cable tension calibration by OTIS engineers today between 02:00 PM and 05:00 PM. Please utilize Service Lift No. 1 during this period. We apologize for any inconvenience.",
              timestamp = now - (14 * hour),
              category = "Maintenance",
              priority = "Normal",
              author = "Technical & Elevator Support Team",
              isPinned = false
            ),
            CommunityNotice(
              title = "Society Monsoon Gardening & Pest Control Drive",
              description = "Comprehensive mosquito fogging and herbal pest control will be executed across all podium gardens, basement parking levels B1/B2, and perimeter walkways. Please keep balcony windows closed during evening fogging (5:30 PM - 7:00 PM).",
              timestamp = now - (1 * day + 3 * hour),
              category = "Environment",
              priority = "Normal",
              author = "Horticulture & Sanitization Committee",
              isPinned = false
            ),
            CommunityNotice(
              title = "Security Protocol: Mandatory QR Gate Passes for Moving Cabs",
              description = "To ensure resident safety, all external movers, furniture delivery trucks, and non-registered cargo vehicles must obtain an automated digital gate pass via the BestNet App prior to arrival. Gate 2 will remain designated exclusively for heavy transport vehicles.",
              timestamp = now - (2 * day + 5 * hour),
              category = "Security",
              priority = "Important",
              author = "Chief Security Officer (Main Gate)",
              isPinned = false
            ),
            CommunityNotice(
              title = "Grand Independence & Cultural Fest Celebrations",
              description = "Join us for our community cultural evening featuring patriotic musical performances, children's art exhibitions, food stalls, and an interactive quiz competition. High tea will be hosted at the central amphitheater at 05:30 PM. All residents and families are warmly welcome.",
              timestamp = now - (4 * day + 2 * hour),
              category = "Cultural",
              priority = "Normal",
              author = "Cultural & Sports Affairs Committee",
              isPinned = false
            ),
            CommunityNotice(
              title = "Solar Rooftop Grid Synchronization Complete",
              description = "We are pleased to announce that our 120kW rooftop solar grid has been energized and synchronized with the state power grid. This initiative is projected to reduce common area electricity tariffs by 35% annually starting this billing cycle.",
              timestamp = now - (7 * day),
              category = "Sustainability",
              priority = "Normal",
              author = "RWA Green Energy Task Force",
              isPinned = false
            )
          )
        )
      }
    }
  }
}
