package edu.gwu.Watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OthersReviewsAdapter(val reviews: MutableList<Review>) : RecyclerView.Adapter<OthersReviewsAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        // How many rows (total) do you want the adapter to render?
        return reviews.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // The RecyclerView needs a "fresh" / new row, so we need to:
        // 1. Read in the XML file for the row type
        // 2. Use the new row to build a ViewHolder to return

        //Step 1
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.row_review, parent, false)

        //Step 2
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // The RecyclerView is ready to display a new (or recycled) row on the screen
        // for position indicated -- override the UI elements with the correct data
        val currentReview = reviews[position]

        holder.email.text = currentReview.email
        holder.score.text = currentReview.score
        holder.review.text = currentReview.review
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val email: TextView = itemView.findViewById(R.id.reviewEmail)
        val score: TextView = itemView.findViewById(R.id.reviewScore)
        val review: TextView = itemView.findViewById(R.id.othersReview)
    }
}