package com.example.mynewsfeed.repositories

import com.example.mynewsfeed.api.RetrofitInstance
import com.example.mynewsfeed.data.db.ArticleDatabase
import com.example.mynewsfeed.data.entities.Article

class NewsRepository(
  private val db: ArticleDatabase
) {
  suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
    RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

  suspend fun searchNews(searchQuery: String, pageNumber: Int) =
    RetrofitInstance.api.searchNews(searchQuery, pageNumber)

  suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

  fun getSavedNews() = db.getArticleDao().getAllArticles()

  suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

  suspend fun isNewsExistInDB(url: String) = db.getArticleDao().isNewsExistInDB(url)
}