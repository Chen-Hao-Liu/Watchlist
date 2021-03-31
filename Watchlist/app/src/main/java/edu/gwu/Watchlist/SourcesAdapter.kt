package edu.gwu.Watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SourcesAdapter(val sourcesList: MutableList<Source>, val mContext: Context, val media: String) : RecyclerView.Adapter<SourcesAdapter.ViewHolder>() {
    private var sources: MutableList<Source> = sourcesList

    fun updateAdapter(newList : List<Source>) {
        for(source in newList){
            sources.add(source)
        }
    }
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
        val itemView: View = layoutInflater.inflate(R.layout.row_source, parent, false)

        //Step 2
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // The RecyclerView is ready to display a new (or recycled) row on the screen
        // for position indicated -- override the UI elements with the correct data
        val currentSource = sources[position]

        holder.title.text = currentSource.title
        holder.score.text = currentSource.score
        holder.synopsis.text = currentSource.synopsis

        // different layout depending on anime or manga
        if(media == "anime") {
            holder.showRating.visibility = View.VISIBLE
            holder.showRating.text = currentSource.rated
            holder.episodes.text = currentSource.episodes
        } else {
            holder.showRating.visibility = View.INVISIBLE
            holder.episodeNumber.text = "Vols: " + currentSource.volumes
            holder.episodes.text = "Chapters: " + currentSource.chapters
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
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.seriesTitle)
        val score: TextView = itemView.findViewById(R.id.score)
        val synopsis: TextView = itemView.findViewById(R.id.synopsis)
        val showRating: TextView = itemView.findViewById(R.id.showRating)
        val image: ImageView = itemView.findViewById(R.id.topImage)
        val episodes: TextView = itemView.findViewById(R.id.episodes)
        val episodeNumber: TextView = itemView.findViewById(R.id.episodeNumber)
    }
}