package com.agroatlautla.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        CropEntity::class,
        CalendarActivityEntity::class,
        PestEntity::class,
        ExpenseEntity::class,
        SyncQueueEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AgroDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cropDao(): CropDao
    abstract fun calendarActivityDao(): CalendarActivityDao
    abstract fun pestDao(): PestDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var instance: AgroDatabase? = null

        private val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `concept` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `needsSync` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `crops_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `stage` TEXT NOT NULL,
                        `sowDate` TEXT NOT NULL,
                        `surfaceArea` TEXT NOT NULL,
                        `irrigationType` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `nextActivity` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `needsSync` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `crops_new` (
                        `id`,`name`,`stage`,`icon`,`updatedAt`,`needsSync`,
                        `sowDate`,`surfaceArea`,`nextActivity`,`irrigationType`,`notes`
                    )
                    SELECT `id`,`name`,`stage`,`icon`,`updatedAt`,`needsSync`,
                        CASE WHEN `areaLabel` LIKE '% - %'
                            THEN substr(`areaLabel`, 1, instr(`areaLabel`, ' - ') - 1)
                            ELSE `areaLabel` END,
                        CASE WHEN `areaLabel` LIKE '% - %'
                            THEN substr(`areaLabel`, instr(`areaLabel`, ' - ') + 3)
                            ELSE 'Sin superficie asignada' END,
                        `riskLabel`,
                        'Temporal (lluvia)',
                        ''
                    FROM `crops`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `crops`")
                db.execSQL("ALTER TABLE `crops_new` RENAME TO `crops`")
                db.execSQL("UPDATE `sqlite_sequence` SET name = 'crops' WHERE name = 'crops_new'")
            }
        }

        private val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                rebuildWithTextIds(
                    db = db,
                    table = "crops",
                    columns = """
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `stage` TEXT NOT NULL,
                        `sowDate` TEXT NOT NULL,
                        `surfaceArea` TEXT NOT NULL,
                        `irrigationType` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `nextActivity` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `needsSync` INTEGER NOT NULL
                    """,
                    select = "`id`,`name`,`stage`,`sowDate`,`surfaceArea`,`irrigationType`,`notes`,`nextActivity`,`icon`,`updatedAt`,`needsSync`"
                )
                rebuildWithTextIds(
                    db = db,
                    table = "calendar_activities",
                    columns = """
                        `id` TEXT NOT NULL,
                        `day` INTEGER NOT NULL,
                        `month` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `cropName` TEXT NOT NULL,
                        `colorTag` TEXT NOT NULL,
                        `needsSync` INTEGER NOT NULL
                    """,
                    select = "`id`,`day`,`month`,`type`,`title`,`cropName`,`colorTag`,`needsSync`"
                )
                rebuildWithTextIds(
                    db = db,
                    table = "pests",
                    columns = """
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `affectedCrop` TEXT NOT NULL,
                        `severity` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    """,
                    select = "`id`,`name`,`affectedCrop`,`severity`,`description`,`updatedAt`"
                )
                rebuildWithTextIds(
                    db = db,
                    table = "expenses",
                    columns = """
                        `id` TEXT NOT NULL,
                        `concept` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `needsSync` INTEGER NOT NULL
                    """,
                    select = "`id`,`concept`,`amount`,`date`,`category`,`needsSync`"
                )
            }

            private fun rebuildWithTextIds(
                db: SupportSQLiteDatabase,
                table: String,
                columns: String,
                select: String
            ) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${table}_new` (
                        $columns,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("INSERT INTO `${table}_new` ($select) SELECT CAST(`id` AS TEXT), ${select.substringAfter("`id`,")} FROM `${table}`")
                db.execSQL("DROP TABLE `${table}`")
                db.execSQL("ALTER TABLE `${table}_new` RENAME TO `${table}`")
            }
        }

        fun getDatabase(context: Context): AgroDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AgroDatabase::class.java,
                    "agroatlautla.db"
                )
                    .addMigrations(Migration1To2, Migration2To3, Migration3To4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
