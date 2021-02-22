package com.example.mynewsfeed.data.db

import android.content.Context
import androidx.room.*
import com.example.mynewsfeed.data.entities.Article

@Database(
  entities = [Article::class],
  version = 1
)
@TypeConverters(Converter::class)
abstract class ArticleDatabase : RoomDatabase() {

  abstract fun getArticleDao(): ArticleDAO

  companion object {
    @Volatile
    private var instance: ArticleDatabase? = null
    private val LOCK = Any()

    // call every time we create instance of ArticleDatabase()
    operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
      instance ?: createDatabase(context).also { instance = it }
    }

    private fun createDatabase(context: Context) =
      Room.databaseBuilder(
        context.applicationContext,
        ArticleDatabase::class.java,
        "article_db.db"
      ).build()
  }
}