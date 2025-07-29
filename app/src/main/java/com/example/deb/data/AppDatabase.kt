// app/src/main/java/com/example/deb/data/AppDatabase.kt
package com.example.deb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, ChatMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** 대화 세션 테이블 접근 DAO */
    abstract fun conversationDao(): ConversationDao

    /** 채팅 메시지 테이블 접근 DAO */
    abstract fun chatDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** 싱글턴으로 데이터베이스 인스턴스 반환 */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deb-chat-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
