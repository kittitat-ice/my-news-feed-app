package com.example.mynewsfeed.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.example.mynewsfeed.NewsApplication
import com.example.mynewsfeed.R
import com.example.mynewsfeed.databinding.FragmentArticleBinding
import com.example.mynewsfeed.view.NewsActivity
import com.example.mynewsfeed.view.viewModel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ArticleFragment : Fragment(R.layout.fragment_article) {

  lateinit var viewModel: NewsViewModel
  private val args: ArticleFragmentArgs by navArgs()
  private var _binding: FragmentArticleBinding? = null
  private val binding get() = _binding!!
  private lateinit var navController: NavController

  val TAG = "ArticleFragment"

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = FragmentArticleBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = (activity as NewsActivity).viewModel

    val article = args.article
    binding.webView.apply {
      webViewClient = WebViewClient()
      loadUrl(article.url)
    }

    binding.fabFavorite.setOnClickListener{
      viewModel.saveArticle(article)
      Snackbar.make(view, "Article saved!", Snackbar.LENGTH_SHORT).show()
      it.visibility = View.GONE
    }

    lifecycleScope.launch {
      val isExist = viewModel.isNewsAlreadySaved(article.url)
      binding.fabFavorite.visibility = if(isExist) View.GONE else View.VISIBLE
    }

    (activity as NewsActivity).supportActionBar?.title = article.source?.name
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}