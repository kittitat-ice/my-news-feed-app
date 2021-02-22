package com.example.mynewsfeed

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import com.example.mynewsfeed.data.db.ArticleDatabase
import com.example.mynewsfeed.repositories.NewsRepository
import com.example.mynewsfeed.view.viewModel.NewsViewModelProviderFactory
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule

class NewsApplication : Application(), DIAware {
  override val di: DI = DI.lazy {
    import(androidXModule(this@NewsApplication))
    bind() from singleton { ArticleDatabase(instance()) }
    bind() from singleton { NewsRepository(instance()) }
    bind() from provider { NewsViewModelProviderFactory(this@NewsApplication , instance()) }
  }
}