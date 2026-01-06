package com.zash3dit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zash3dit.data.local.dao.AudioClipDao
import com.zash3dit.data.local.dao.EditProjectDao
import com.zash3dit.data.local.dao.TextOverlayDao
import com.zash3dit.data.local.dao.VideoClipDao
import com.zash3dit.data.local.entity.AudioClipEntity
import com.zash3dit.data.local.entity.EditProjectEntity
import com.zash3dit.data.local.entity.TextOverlayEntity
import com.zash3dit.data.local.entity.VideoClipEntity

@Database(
    entities = [
        EditProjectEntity::class,
        VideoClipEntity::class,
        AudioClipEntity::class,
        TextOverlayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun editProjectDao(): EditProjectDao
    abstract fun videoClipDao(): VideoClipDao
    abstract fun audioClipDao(): AudioClipDao
    abstract fun textOverlayDao(): TextOverlayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "video_editor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}