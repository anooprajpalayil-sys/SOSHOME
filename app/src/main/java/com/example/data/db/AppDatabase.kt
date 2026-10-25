package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.EmergencyEvent
import com.example.data.model.SafetyCircleMember
import com.example.data.model.TrustedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyEventDao {
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EmergencyEvent?

    @Query("SELECT * FROM emergency_events WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveEvent(): Flow<EmergencyEvent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEvent)

    @Update
    suspend fun updateEvent(event: EmergencyEvent)

    @Delete
    suspend fun deleteEvent(event: EmergencyEvent)

    @Query("DELETE FROM emergency_events")
    suspend fun clearAllEvents()
}

@Dao
interface TrustedContactDao {
    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<TrustedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContact): Long

    @Update
    suspend fun updateContact(contact: TrustedContact)

    @Delete
    suspend fun deleteContact(contact: TrustedContact)

    @Query("SELECT COUNT(*) FROM trusted_contacts")
    suspend fun getContactCount(): Int
}

@Dao
interface SafetyCircleDao {
    @Query("SELECT * FROM safety_circle_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<SafetyCircleMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SafetyCircleMember)

    @Update
    suspend fun updateMember(member: SafetyCircleMember)

    @Delete
    suspend fun deleteMember(member: SafetyCircleMember)

    @Query("SELECT COUNT(*) FROM safety_circle_members")
    suspend fun getMemberCount(): Int
}

@Database(
    entities = [EmergencyEvent::class, TrustedContact::class, SafetyCircleMember::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyEventDao(): EmergencyEventDao
    abstract fun trustedContactDao(): TrustedContactDao
    abstract fun safetyCircleDao(): SafetyCircleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safevoice_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
