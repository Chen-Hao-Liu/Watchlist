package edu.gwu.Watchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.doAsync
import java.lang.Exception

class ResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sourceManager: SourceManager
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SourcesAdapter
    private lateinit var searchTerm: String
    private lateinit var media: String
    private lateinit var type: String
    private lateinit var status: String
    private lateinit var orderBy: String
    private lateinit var genre: String
    private lateinit var rated: String
    private lateinit var progressBar: ProgressBar
    private lateinit var navBar: BottomNavigationView
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

        navBar = findViewById(R.id.navBar_Results)
        recyclerView = findViewById(R.id.resultsRecycler)
        progressBar = findViewById(R.id.progressBar)
        sourceManager = SourceManager()

        title = "$media " + getString(R.string.title_results_activity) + " '$searchTerm'"

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

                    progressBar.visibility = View.VISIBLE
                    sourceSelection()
                }
            }
        })

        navBar.setSelectedItemId(R.id.action_find)
        navBar.setOnNavigationItemSelectedListener { item->
            when(item.itemId){
                R.id.action_find ->{
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_list ->{
                    val intent = Intent(this, ListActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_top -> {
                    val intent = Intent(this, TopActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_profile ->{
                    val intent = Intent(this, LogoutActivity::class.java)
                    startActivity(intent)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
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
                        var notification = "$searchTerm: " + getString(R.string.noResults)
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
                Log.e("ResultsActivity", getString(R.string.jikanFail), exception)
                Toast.makeText(
                    this@ResultsActivity,
                    getString(R.string.jikanFail) + " : $exception",
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