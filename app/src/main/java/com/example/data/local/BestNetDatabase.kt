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
          // No seed callback: the database starts empty and is filled from the server.
          .build()
        INSTANCE = instance
        instance
      }
    }

    // Seeding was removed.
    //
    // This callback used to populate residents, complaints, visitors, notices
    // and community notices with sample data on first launch — including a
    // fake identity ("Rahul Sharma", A-1201, Sunrise Apartments). Every one of
    // those tables now has a real server source and is replaced on sync, so the
    // sample rows only ever appeared *alongside* the resident's own data:
    // somebody else's name before the first sync landed, and sample complaints
    // mixed in with real tickets afterwards.
    //
    // An empty database on first launch is correct — the app requires login,
    // and login syncs before the shell is shown.
  }
}
