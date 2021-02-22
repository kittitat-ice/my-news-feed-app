package com.example.mynewsfeed.view.viewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mynewsfeed.NewsApplication
import com.example.mynewsfeed.data.entities.Article
import com.example.mynewsfeed.data.entities.NewsResponse
import com.example.mynewsfeed.repositories.NewsRepository
import com.example.mynewsfeed.utils.Constants.Companion.COUNTRY_CODE
import com.example.mynewsfeed.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
  app: Application,
  private val repository: NewsRepository
) : AndroidViewModel(app) {
  val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
  var breakingNewsPage = 1
  var breakingNewsResponse: NewsResponse? = null

  val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
  var searchNewsPage = 1
  var prevQuery: String? = null
  var searchNewsResponse: NewsResponse? = null

  init {
    getBreakingNews(COUNTRY_CODE)
  }

  fun getBreakingNews(countryCode: String) = viewModelScope.launch {
    safeBreakingNewsCall(countryCode)
  }

  fun searchNews(searchQuery: String) = viewModelScope.launch {
    safeSearchNewsCall(searchQuery)
  }

  private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
    if (response.isSuccessful) {
      response.body()?.let { result ->
        breakingNewsPage++
        if (breakingNewsResponse == null) {
          breakingNewsResponse = result
        } else {
          val oldArticles = breakingNewsResponse?.articles
          val newArticles = result.articles
          oldArticles?.addAll(newArticles)
        }
        return Resource.Success(breakingNewsResponse ?: result)
      }
    }
    return Resource.Error(response.message())
  }


  private fun handleSearchNewsResponse(
    response: Response<NewsResponse>,
    searchQuery: String
  ): Resource<NewsResponse> {
    if (response.isSuccessful) {
      response.body()?.let { result ->
        searchNewsPage++
        prevQuery = searchQuery
        if (searchNewsResponse == null) {
          searchNewsResponse = result
        } else {
          val oldArticles = searchNewsResponse?.articles
          val newArticles = result.articles
          oldArticles?.addAll(newArticles)
        }
        return Resource.Success(searchNewsResponse ?: result)
      }
    }
    return Resource.Error(response.message())
  }

  fun saveArticle(article: Article) = viewModelScope.launch {
    repository.upsert(article)
  }

  fun getSavedNews() = repository.getSavedNews()

  fun deleteArticle(article: Article) = viewModelScope.launch {
    repository.deleteArticle(article)
  }

  private suspend fun safeBreakingNewsCall(countryCode: String) {
    breakingNews.postValue(Resource.Loading())
    try {
      if (hasInternetConnection()) {
        val response = repository.getBreakingNews(countryCode, breakingNewsPage)
        breakingNews.postValue(handleBreakingNewsResponse(response))
      } else {
        breakingNews.postValue(Resource.Error("No internet connection"))
      }
    } catch (t: Throwable) {
      when (t) {
        is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
        else -> breakingNews.postValue(Resource.Error("Conversion Error"))
      }
    }
  }

  private suspend fun safeSearchNewsCall(searchQuery: String) {
    searchNews.postValue(Resource.Loading())
    if (prevQuery != searchQuery) {
      searchNewsPage = 1
      searchNewsResponse = null
    }
    try {
      if (hasInternetConnection()) {
        val response = repository.searchNews(searchQuery, searchNewsPage)
        searchNews.postValue(handleSearchNewsResponse(response, searchQuery))
      } else {
        searchNews.postValue(Resource.Error("No internet connection"))
      }
    } catch (t: Throwable) {
      when (t) {
        is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
        else -> searchNews.postValue(Resource.Error("Conversion Error"))
      }

    }
  }

  private fun hasInternetConnection(): Boolean {
    val connectivityManager = getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          val activeNetwork = connectivityManager.activeNetwork ?: return false
          val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
          return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
          }
        } else {
          connectivityManager.activeNetworkInfo?.run {
            return when (type) {
              TYPE_WIFI -> true
              TYPE_MOBILE -> true
              TYPE_ETHERNET -> true
              else -> false
            }
          }
        }
    return false
  }

  suspend fun isNewsAlreadySaved(url: String) = repository.isNewsExistInDB(url)
}