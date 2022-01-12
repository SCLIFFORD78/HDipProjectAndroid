package ie.wit.hive.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ie.wit.hive.models.HiveModel


@Database(entities = [HiveModel::class], version = 2,  exportSchema = false)
@TypeConverters(ie.wit.hive.helpers.Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun hiveDao(): HiveDao
}