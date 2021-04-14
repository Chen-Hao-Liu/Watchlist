package edu.gwu.Watchlist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ListAdapter(val sources: MutableList<Source>, val mContext: Context, val media: String) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        // How many rows (total) do you want the adapter to render?
        return sources.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // The RecyclerView needs a "fresh" / new row, so we need to:
        // 1. Read in the XML file for the row type
        // 2. Use the new row to build a ViewHolder to return

        //Step 1
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.row_saved, parent, false)

        //Step 2
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // The RecyclerView is ready to display a new (or recycled) row on the screen
        // for position indicated -- override the UI elements with the correct data
        val currentSource = sources[position]

        holder.title.text = currentSource.title
        holder.type.text = currentSource.type
        holder.members.text = currentSource.members
        holder.score.text = currentSource.score
        holder.yourScore.text = currentSource.userScore

        // Change based on whether or not user score is set
        if(currentSource.userScore != "N/A"){
            holder.yourScoreTitle.text = "Your Score"
            holder.yourScore
                .setCompoundDrawablesWithIntrinsicBounds(
                    mContext.getDrawable(android.R.drawable.btn_star_big_on),
                    null,
                    null,
                    null
                )
            holder.yourScore.setTextColor(Color.BLACK)
            holder.yourScoreTitle.setTextColor(Color.BLACK)
        }else{
            holder.yourScoreTitle.text = "Not Scored"
        }

        // different layout depending on anime or manga
        if(media == "anime") {
            holder.num.text = "(" + currentSource.episodes + "eps)"
        } else {
            holder.num.text = "(Vols: " + currentSource.volumes + " Chapters: " + currentSource.chapters + ")"
        }

        // Picasso load image
        if(!currentSource.image_url.isNullOrBlank()){
            Picasso
                .get()
                .setIndicatorsEnabled(true)
            Picasso
                .get()
                .load(currentSource.image_url)
                .into(holder.image)
        }

        // To be implemented
        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(mContext, ReviewActivity::class.java)
            intent.putExtra("media", media)
            intent.putExtra("source", currentSource)
            mContext.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.savedTitle)
        val type: TextView = itemView.findViewById(R.id.savedType)
        val num: TextView = itemView.findViewById(R.id.savedEpisode)
        val members: TextView = itemView.findViewById(R.id.members)
        val score: TextView = itemView.findViewById(R.id.audScore)
        val yourScoreTitle: TextView = itemView.findViewById(R.id.yourScoreTitle)
        val yourScore: TextView = itemView.findViewById(R.id.yourScore)
        val image: ImageView = itemView.findViewById(R.id.saveImage)
    }
}