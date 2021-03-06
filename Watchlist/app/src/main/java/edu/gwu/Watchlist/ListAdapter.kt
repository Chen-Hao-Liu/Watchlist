package edu.gwu.Watchlist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

        val mal_id = currentSource.mal_id
        holder.title.text = currentSource.title
        holder.type.text = currentSource.type
        holder.members.text = currentSource.members
        holder.score.text = currentSource.score
        holder.yourScore.text = currentSource.userScore

        // Change based on whether or not user score is set
        if(currentSource.userScore != "N/A"){
            holder.yourScoreTitle.text = mContext.getString(R.string.yourScoreTitle)
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
            holder.yourScoreTitle.text = mContext.getString(R.string.notScoreTitle)
        }

        // different layout depending on anime or manga
        if(media == "anime") {
            holder.num.text = "(" + currentSource.episodes + " eps)"
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

        // Initialize the favorite button as not selected
        holder.imageBtn.tag = false
        holder.imageBtn.background =
            AppCompatResources.getDrawable(mContext, R.drawable.favorite_unselected)

        // Acquire Firebase instances
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val instance = FirebaseDatabase.getInstance()

        // Acquire saved source if it exists
        val refA = instance.reference.child("/MyList/$uid/$media/$mal_id")
        refA.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.d("ListAdapter", mContext.getString(R.string.FBRetrieve), error.toException())
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
                    holder.imageBtn.background =
                        AppCompatResources.getDrawable(mContext, R.drawable.favorite_selected)
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
                        val removed = "${currentSource.title}: " + mContext.getString(R.string.removed)
                        snapshot.ref.removeValue().addOnSuccessListener {
                            Log.d("ListAdapter", removed)
                            Toast.makeText(
                                mContext,
                                removed,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val failed = "${currentSource.title}: " + mContext.getString(R.string.failRemove) + " " + databaseError
                        Log.d("ListAdapter", failed)
                    }
                })

                // Remove from saved list
                val refC = instance.reference.child("/MyList/$uid/$media/$mal_id")
                refC.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val removed = "${currentSource.title}: " + mContext.getString(R.string.removeList)
                        snapshot.ref.removeValue().addOnSuccessListener {
                            // Set favorite to unselected.
                            holder.imageBtn.tag = false
                            holder.imageBtn.background = AppCompatResources.getDrawable(
                                mContext,
                                R.drawable.favorite_unselected
                            )
                            Log.d("ListAdapter", removed)
                            Toast.makeText(
                                mContext,
                                removed,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val failed = "${currentSource.title}: " + mContext.getString(R.string.failRemoveList) + " " + databaseError
                        Log.d("ListAdapter",failed)
                    }
                })
            } else {
                // Attempts to add to favorite
                val refD = instance.reference.child("/MyList/$uid/$media/$mal_id")
                refD.setValue(currentSource)
                    .addOnSuccessListener {
                        val saved = "${currentSource.title}: " + mContext.getString(R.string.saved)
                        Log.d("ListAdapter", saved)
                        Toast.makeText(
                            mContext,
                            saved,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener {
                        val failedSave = "${currentSource.title}: " + mContext.getString(R.string.failedSave)
                        Log.d("ListAdapter", failedSave)
                        Toast.makeText(
                            mContext,
                            failedSave,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                holder.imageBtn.tag = true
                holder.imageBtn.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.favorite_selected)
            }
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
        val imageBtn: ImageButton = itemView.findViewById(R.id.favSaved)
    }
}