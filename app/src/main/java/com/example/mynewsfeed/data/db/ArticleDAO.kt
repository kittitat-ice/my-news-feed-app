package com.example.mynewsfeed.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynewsfeed.data.entities.Article

@Dao
interface ArticleDAO {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(article: Article): Long

  @Query("SELECT * FROM articles")
  fun getAllArticles(): LiveData<List<Article>>

  @Delete
  suspend fun deleteArticle(article: Article)

  @Query("SELECT EXISTS(SELECT * FROM articles WHERE url = :url)")
  suspend fun isNewsExistInDB(url : String): Boolean
}