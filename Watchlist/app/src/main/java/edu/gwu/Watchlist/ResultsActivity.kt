package edu.gwu.Watchlist

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync
import java.lang.Exception

class ResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sourceManager: SourceManager
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SourcesAdapter
    private lateinit var savedState: Parcelable
    private lateinit var searchTerm: String
    private lateinit var media: String
    private lateinit var type: String
    private lateinit var status: String
    private lateinit var orderBy: String
    private lateinit var genre: String
    private lateinit var rated: String
    private lateinit var progressBar: ProgressBar
    private var page: Int = 1
    // load is a flag that prevents users from loading a new page while currently loading a new page
    // meaning if they scroll to refresh, and the progressBar is still going on, scrolling again won't
    // do anything.
    private var load: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Retrieve the resources needed for API call
        val intent = getIntent()
        searchTerm = intent.getStringExtra("searchTerm")!!
        media = intent.getStringExtra("media")!!
        type = intent.getStringExtra("type")!!
        status = intent.getStringExtra("status")!!
        orderBy = intent.getStringExtra("orderBy")!!
        genre = intent.getStringExtra("genre")!!
        rated = intent.getStringExtra("rated")!!

        recyclerView = findViewById(R.id.resultsRecycler)
        progressBar = findViewById(R.id.progressBar)
        sourceManager = SourceManager()

        title = "Search $media for '$searchTerm'"

        // Retrieve first page
        progressBar.visibility = View.VISIBLE
        sourceSelection()

        // infinite scroll implementation
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition() + 1
                val total = recyclerView.layoutManager!!.itemCount

                // If user is attempting to scroll past final view
                if (total == lastPosition && load) {
                    load = false
                    Log.d("ResultsActivity", "Position: $lastPosition, Total: $total, Page: $page")
                    // Save position
                    savedState = recyclerView.layoutManager?.onSaveInstanceState()!!
                    progressBar.visibility = View.VISIBLE
                    sourceSelection()
                }
            }
        })
    }

    fun sourceSelection(){
        doAsync{
            Thread.sleep(2000)
            val sources = sourceManager.retrieveSources(searchTerm, media, type, status, orderBy, genre, rated, page)

            try{
                if(sources.isNotEmpty()) {
                    runOnUiThread {
                        // Initial page
                        if (page == 1) {
                            adapter = SourcesAdapter(sources.toMutableList(), this@ResultsActivity, media)
                            recyclerView.adapter = adapter
                            layoutManager = LinearLayoutManager(this@ResultsActivity)
                            recyclerView.layoutManager = layoutManager
                            // Further paging
                        } else {
                            adapter.updateAdapter(sources)
                            adapter.notifyDataSetChanged()
                            // Restore position
                            recyclerView.layoutManager?.onRestoreInstanceState(savedState)
                        }
                        // End progress bar
                        progressBar.visibility = View.INVISIBLE
                        // allow load
                        load = true
                        // Increment page to fetch next set of results
                        page++
                    }
                }else{
                    runOnUiThread {
                        var notification = "Search for $searchTerm yielded no further results"
                        Log.d("ResultsActivity", notification)
                        val toast = Toast.makeText(
                            this@ResultsActivity,
                            notification,
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        // End progress bar
                        progressBar.visibility = View.INVISIBLE
                        // allow load
                        load = true
                    }
                }
            }catch(exception: Exception){
                Log.e("ResultsActivity", "Jikan API failed!", exception)
                Toast.makeText(
                    this@ResultsActivity,
                    "Jikan API failed! : $exception",
                    Toast.LENGTH_LONG
                ).show()

                // End progress bar
                progressBar.visibility = View.INVISIBLE
                // allow load
                load = true
            }
        }
    }
}