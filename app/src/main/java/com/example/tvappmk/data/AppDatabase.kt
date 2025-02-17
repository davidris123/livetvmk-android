package com.example.tvappmk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tvappmk.model.Channel

@Database(entities = [Channel::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun channelDao(): ChannelDao

    //static singleton object koj ke ja pretstavuva bazata na podatoci
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "channels"

        @JvmStatic //oznaka za java deka e static
        fun getDatabase(context: Context): AppDatabase { //builder na singleton objektot
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, //kontekstot na aplikacijata
                    AppDatabase::class.java, // klasata koja ke ja koristime za da ja kreirame bazata
                    DATABASE_NAME //ime na bazata
                ).build()
                INSTANCE = instance //dodeluvame vrednost na instancata
                instance //singleton f-jata sekogas ke vrakja 1 instanca od bazata
            }
        }
    }
}
