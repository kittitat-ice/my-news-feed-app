package com.example.mynewsfeed.api

import com.example.mynewsfeed.data.entities.NewsResponse
import com.example.mynewsfeed.utils.Constants.Companion.API_KEY
import com.example.mynewsfeed.utils.Constants.Companion.COUNTRY_CODE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

  // get top news by country
  @GET("v2/top-headlines")
  suspend fun getBreakingNews(
    @Query("country")
    countryCode: String = COUNTRY_CODE,
    @Query("page")
    pageNumber: Int = 1,
    @Query("apiKey")
    apiKey: String = API_KEY
  ): Response<NewsResponse>

  // get new from search query
  @GET("v2/everything")
  suspend fun searchNews(
    @Query("q")
    searchQuery: String,
    @Query("page")
    pageNumber: Int = 1,
    @Query("apiKey")
    apiKey: String = API_KEY
  ): Response<NewsResponse>
}