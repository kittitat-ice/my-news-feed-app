package com.example.mynewsfeed.view.fragments

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsfeed.R
import com.example.mynewsfeed.databinding.FragmentSavedNewsBinding
import com.example.mynewsfeed.view.NewsActivity
import com.example.mynewsfeed.view.adapter.NewsAdapter
import com.example.mynewsfeed.view.viewModel.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

  lateinit var viewModel: NewsViewModel
  lateinit var newsAdapter: NewsAdapter
  private var _binding: FragmentSavedNewsBinding? = null
  private val binding get() = _binding!!

  val TAG = "SavedNewsFragment"

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentSavedNewsBinding.inflate(inflater, container, false)

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
        R.id.action_savedNewsFragment_to_articleFragment,
        bundle
      )
    }

    // handle swipe to delete
    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
      0,
      ItemTouchHelper.LEFT
    ) {
      override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
      ): Boolean {
        return true
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        val article = newsAdapter.differ.currentList[position]
        viewModel.deleteArticle(article)
        Snackbar.make(view, "Article Deleted!", Snackbar.LENGTH_LONG).apply {
          setAction("Undo") {
            viewModel.saveArticle(article)
          }
          show()
        }
      }

      override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
      ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val background = ColorDrawable(Color.RED)
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 50f
        if (dX > 0) { // Swiping to the right
          background.setBounds(
            itemView.left, itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
          )
          background.draw(c)
          c.drawText("Delete", (itemView.left + 50).toFloat(),
            (itemView.top + itemView.height / 2).toFloat(), paint)
        } else if (dX < 0) { // Swiping to the left
          background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top, itemView.right, itemView.bottom
          )
          background.draw(c)
          c.drawText("Delete", (itemView.right - 200).toFloat(),
            (itemView.top + itemView.height / 2).toFloat(), paint)
        } else { // view is unSwiped
          background.setBounds(0, 0, 0, 0)
        }
      }
    }

    ItemTouchHelper(itemTouchHelperCallback).apply {
      attachToRecyclerView(binding.rvSavedNews)
    }

    // observe change
    viewModel.getSavedNews().observe(viewLifecycleOwner, { articles ->
      // if different then update adapter
      newsAdapter.differ.submitList(articles)
    })
  }

  private fun setupRecyclerView() {
    newsAdapter = NewsAdapter()
    binding.rvSavedNews.apply {
      adapter = newsAdapter
      layoutManager = LinearLayoutManager(activity)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}