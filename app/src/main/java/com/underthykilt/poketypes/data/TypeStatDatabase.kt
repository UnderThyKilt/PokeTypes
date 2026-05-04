package com.underthykilt.poketypes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TypeStatEntity::class], version = 1, exportSchema = false)
abstract class TypeStatDatabase : RoomDatabase() {
    abstract fun typeStatDao(): TypeStatDao

    companion object {
        @Volatile private var INSTANCE: TypeStatDatabase? = null

        fun get(context: Context): TypeStatDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, TypeStatDatabase::class.java, "type_stats")
                .build()
                .also { INSTANCE = it }
        }
    }
}
