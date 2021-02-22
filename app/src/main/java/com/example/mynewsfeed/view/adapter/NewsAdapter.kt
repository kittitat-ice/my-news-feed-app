package com.example.mynewsfeed.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsfeed.R
import com.example.mynewsfeed.data.entities.Article
import com.example.mynewsfeed.databinding.ItemArticlePreviewBinding
import com.example.mynewsfeed.utils.loadImageFromUrl
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

  inner class ArticleViewHolder(private val binding: ItemArticlePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bindArticle(article: Article) = with(binding) {
      if (article.urlToImage.isNullOrBlank()) {
        ivArticleImage.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_no_image))
        ivArticleImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        ivArticleImage.setBackgroundColor(Color.parseColor("#EEEEEE"))
      } else {
        ivArticleImage.loadImageFromUrl(article.urlToImage)
      }
      tvSource.text = article.source?.name
      tvTitle.text = article.title
      tvDescription.text = article.description
      try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val convertedDate: Date? = sdf.parse(article.publishedAt)
        val formattedDate: String? = SimpleDateFormat("dd MM YYYY HH:mm").format(convertedDate)
        tvPublishedAt.text = formattedDate
      } catch (e: ParseException) {
        e.printStackTrace()
      }
      root.setOnClickListener {
        onItemClickListener?.let { it(article) }
      }
    }
  }

  private val differCallback = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem == newItem
    }
  }

  val differ = AsyncListDiffer(this, differCallback)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
    val binding = ItemArticlePreviewBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
    return ArticleViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
    val article = differ.currentList[position]
    holder.bindArticle(article)
  }

  override fun getItemCount(): Int {
    return differ.currentList.size
  }

  private var onItemClickListener: ((Article) -> Unit)? = null

  fun setOnItemClickListener(listener: (Article) -> Unit) {
    onItemClickListener = listener
  }
}