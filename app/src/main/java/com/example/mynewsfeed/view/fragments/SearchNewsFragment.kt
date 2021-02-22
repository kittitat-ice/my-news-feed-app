package com.example.mynewsfeed.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsfeed.R
import com.example.mynewsfeed.databinding.FragmentSearchNewsBinding
import com.example.mynewsfeed.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.mynewsfeed.utils.Constants.Companion.SEARCH_DELAY
import com.example.mynewsfeed.utils.Resource
import com.example.mynewsfeed.view.NewsActivity
import com.example.mynewsfeed.view.adapter.NewsAdapter
import com.example.mynewsfeed.view.viewModel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

  lateinit var viewModel: NewsViewModel
  lateinit var newsAdapter: NewsAdapter
  private var _binding: FragmentSearchNewsBinding? = null
  private val binding get() = _binding!!
  private var isLoading = false
  private var isLastPage = false
  private var isScrolling = false

  val TAG = "SearchNewsFragment"

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = (activity as NewsActivity).viewModel
    setupRecyclerView()

    newsAdapter.setOnItemClickListener {
      val bundle = Bundle().apply {
        putSerializable("article", it)
      }
      findNavController().navigate(
        R.id.action_searchNewsFragment_to_articleFragment,
        bundle
      )
    }

    //
    var job: Job? = null
    binding.etSearch.addTextChangedListener { editable ->
      job?.cancel()
      job = MainScope().launch {
        delay(SEARCH_DELAY)
        editable?.let {

          if (editable.toString().isNotBlank()) {
            viewModel.searchNews(editable.toString())
          }
        }
      }
    }

    viewModel.searchNews.observe(viewLifecycleOwner, { response ->
      when(response) {
        is Resource.Success -> {
          binding.srlRefreshSearchNews.isEnabled = true
          hideProgressBar()
          response.data?.let { newsResponse ->
            newsAdapter.differ.submitList(newsResponse.articles.toList())
            val divide = newsResponse.totalResults.toDouble() / QUERY_PAGE_SIZE.toDouble()
            val totalPages: Int = ceil(divide).toInt()
            isLastPage = (viewModel.searchNewsPage - 1) == totalPages
          }
        }
        is Resource.Error -> {
          hideProgressBar()
          response.message?.let { message ->
            Log.e(TAG, "An error has occurred: $message")
            Toast.makeText(activity, "An error has occurred: $message", Toast.LENGTH_LONG).show()
          }
        }
        is Resource.Loading -> {
          showProgressBar()
        }
      }
    })

    binding.srlRefreshSearchNews.isEnabled = false
    binding.srlRefreshSearchNews.setOnRefreshListener {
      viewModel.searchNews(viewModel.prevQuery!!)
      binding.srlRefreshSearchNews.isRefreshing = false
    }
  }

  private fun hideProgressBar() {
    binding.paginationProgressBar.visibility = View.INVISIBLE
    isLoading = false
  }
  private fun showProgressBar() {
    binding.paginationProgressBar.visibility = View.VISIBLE
    isLoading = true
  }

  private fun setupRecyclerView() {
    newsAdapter = NewsAdapter()
    binding.rvSearchNews.apply {
      adapter = newsAdapter
      layoutManager = LinearLayoutManager(activity)
      addOnScrollListener(this@SearchNewsFragment.scrollListener)
    }
  }

  val scrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      super.onScrollStateChanged(recyclerView, newState)
      if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
        isScrolling = true
      }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      super.onScrolled(recyclerView, dx, dy)
      val layoutManager = recyclerView.layoutManager as LinearLayoutManager
      val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
      val visibleItemCount = layoutManager.childCount
      val totalItemCount = layoutManager.itemCount

      val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
      val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
      val isNotAtStart = firstVisibleItemPosition >= 0
      val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
      val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtStart && isTotalMoreThanVisible && isScrolling
      if(shouldPaginate) {
        viewModel.searchNews(binding.etSearch.text.toString())
        isScrolling = false
      }

      Log.d(TAG, "FirstVisiblePos $firstVisibleItemPosition VisibleCount $visibleItemCount TotalCount $totalItemCount Paginate $shouldPaginate")
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}