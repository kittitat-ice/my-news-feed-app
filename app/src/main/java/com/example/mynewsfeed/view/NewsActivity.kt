package com.example.mynewsfeed.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mynewsfeed.R
import com.example.mynewsfeed.databinding.ActivityNewsBinding
import com.example.mynewsfeed.view.viewModel.NewsViewModel
import com.example.mynewsfeed.view.viewModel.NewsViewModelProviderFactory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.android.di

class NewsActivity : AppCompatActivity(), DIAware {

    override val di: DI by di()
    private val factory: NewsViewModelProviderFactory by instance()
    private lateinit var binding: ActivityNewsBinding
    lateinit var navController: NavController
    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, factory).get(NewsViewModel::class.java)
        navController = Navigation.findNavController(this, R.id.newsNavHostFragment)
        binding.bottomNavigationView.setupWithNavController(navController)
        // setOf fragment that are top level and don't need back button
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.breakingNewsFragment, R.id.searchNewsFragment, R.id.savedNewsFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        this.actionBar?.title = navController.currentDestination?.label
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}