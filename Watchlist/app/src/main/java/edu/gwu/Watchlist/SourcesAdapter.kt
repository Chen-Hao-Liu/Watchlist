package edu.gwu.Watchlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SourcesAdapter(sourcesList: MutableList<Source>, val mContext: Context, val media: String) : RecyclerView.Adapter<SourcesAdapter.ViewHolder>() {
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

        val mal_id = currentSource.mal_id
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
            val url = Uri.parse(currentSource.url)
            val intent = Intent(Intent.ACTION_VIEW, url)
            mContext.startActivity(intent)
        }

        // Initialize the favorite button as not selected
        holder.imageBtn.tag = false
        holder.imageBtn.background = getDrawable(mContext, R.drawable.favorite_unselected)

        // Acquire Firebase instances
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val instance = FirebaseDatabase.getInstance()

        // Acquire saved source if it exists
        val refA = instance.reference.child("/MyList/$uid/$media/$mal_id")
        refA.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError){
                Log.d("SourcesAdapter", mContext.getString(R.string.FBConnect), error.toException())
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.FBRetrieve),
                    Toast.LENGTH_LONG
                ).show()
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                val src = snapshot.getValue(Source::class.java)
                // If source exists, set the favorite button to true
                if(src != null){
                    holder.imageBtn.tag = true
                    holder.imageBtn.background = getDrawable(mContext, R.drawable.favorite_selected)
                }
            }
        })

        // Track clicks to the favorite button
        holder.imageBtn.setOnClickListener { v: View ->
            if(holder.imageBtn.tag == true) {
                // Remove the reference from Reviews if it exists
                val refB = instance.reference.child("/Reviews/$media/$mal_id/$uid")
                refB.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue().addOnSuccessListener {
                            val rem = "${currentSource.title}: " + mContext.getString(R.string.removed)
                            Log.d("SourcesAdapter", rem)
                            Toast.makeText(
                                mContext,
                                rem,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(
                            "SourcesAdapter",
                            "${currentSource.title}: " + mContext.getString(R.string.failRemove) + " $databaseError"
                        )
                    }
                })

                // Remove from saved list
                val refC = instance.reference.child("/MyList/$uid/$media/$mal_id")
                refC.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue().addOnSuccessListener {
                            // Set favorite to unselected.
                            holder.imageBtn.tag = false
                            holder.imageBtn.background = getDrawable(mContext, R.drawable.favorite_unselected)
                            val removeSuccess = "${currentSource.title}: " + mContext.getString(R.string.removeList)
                            Log.d("SourcesAdapter", removeSuccess)
                            Toast.makeText(
                                mContext,
                                removeSuccess,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(
                            "SourcesAdapter",
                            "${currentSource.title}: " + mContext.getString(R.string.failRemoveList) + " $databaseError"
                        )
                    }
                })
            } else {
                // Attempts to add to favorite
                val refD = instance.reference.child("/MyList/$uid/$media/$mal_id")
                refD.setValue(currentSource)
                    .addOnSuccessListener {
                        val saveSuccess = "${currentSource.title}: " + mContext.getString(R.string.saved)
                        Log.d("SourceAdapter", saveSuccess)
                        Toast.makeText(
                            mContext,
                            saveSuccess,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener {
                        val saveFail = "${currentSource.title}: " + mContext.getString(R.string.failedSave)
                        Log.d("SourceAdapter", saveFail)
                        Toast.makeText(
                            mContext,
                            saveFail,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                holder.imageBtn.tag = true
                holder.imageBtn.background = getDrawable(mContext, R.drawable.favorite_selected)
            }
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
        val imageBtn: ImageButton = itemView.findViewById(R.id.favorite)
    }
}