package com.example.hackverse.Adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hackverse.DataModels.Comments
import com.example.hackverse.R
import com.example.hackverse.databinding.ItemCommentBinding

class CommentAdapter(private val comments: ArrayList<Comments>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comments) {
            binding.commentUserName.text = comment.commentWriterName
            binding.commentText.text = comment.commentMessage
            Glide.with(binding.root.context)
                .load(comment.commentUserPfpUrl)
                .placeholder(R.drawable.default_picture)
                .into(binding.commentUserPic)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount() = comments.size
}
