package edu.gwu.Watchlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.doAsync
import java.lang.Exception

class TopActivity : AppCompatActivity() {
    private lateinit var spinnerAniMan: Spinner
    private lateinit var spinnerCat: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var topManager: TopManager
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: TopAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var navBar: BottomNavigationView
    private var page: Int = 1
    // load is a flag that prevents users from loading a new page while currently loading a new page
    // meaning if they scroll to refresh, and the progressBar is still going on, scrolling again won't
    // do anything.
    private var load: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        spinnerAniMan = findViewById(R.id.topAniManga)
        spinnerCat = findViewById(R.id.topCategory)
        navBar = findViewById(R.id.navBar_Top)
        recyclerView = findViewById(R.id.topRecycler)
        progressBar = findViewById(R.id.progressBarTop)
        topManager = TopManager()

        // Retrieve first page
        progressBar.visibility = View.VISIBLE

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.aniMangaArr,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerAniMan.adapter = adapter
        }

        // Instantiate adapter for retrieving selected item
        spinnerAniMan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                val category = parent.getItemAtPosition(pos).toString()

                // Use different arrays for anime or manga
                if (category == "anime"){
                    spinnerSetup(R.array.animeCat)
                }else{
                    spinnerSetup(R.array.mangaCat)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Another interface callback
            }
        }

        // infinite scroll implementation
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition() + 1
                val total = recyclerView.layoutManager!!.itemCount

                // If user is attempting to scroll past final view
                if (total == lastPosition && load) {
                    load = false
                    Log.d("TopActivity", "Position: $lastPosition, Total: $total, Page: $page")

                    progressBar.visibility = View.VISIBLE
                    val media : String = spinnerAniMan.selectedItem.toString()
                    val category : String = spinnerCat.selectedItem.toString()
                    sourceSelection(media, category)
                }
            }
        })

        navBar.setSelectedItemId(R.id.action_top)
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

                }
                R.id.action_profile ->{
                    val intent = Intent(this, LogoutActivity::class.java)
                    startActivity(intent)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    fun spinnerSetup(array :Int){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerCat.adapter = adapter
        }

        // Instantiate adapter for retrieving selected item
        spinnerCat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                val category = parent.getItemAtPosition(pos).toString()
                val media : String = spinnerAniMan.selectedItem.toString()
                title = getString(R.string.title_top_activity) + " $category"

                progressBar.visibility = View.VISIBLE
                page = 1
                sourceSelection(media, category)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Another interface callback
            }
        }
    }

    fun sourceSelection(media: String, category: String){
        doAsync{
            Thread.sleep(2000)

            val sources = topManager.retrieveSources(media, category, page)

            try{
                if(sources.isNotEmpty()) {
                    runOnUiThread {
                        // Initial page
                        if (page == 1) {
                            adapter = TopAdapter(sources.toMutableList(), this@TopActivity, media)
                            recyclerView.adapter = adapter
                            layoutManager = LinearLayoutManager(this@TopActivity)
                            recyclerView.layoutManager = layoutManager
                        } else {
                            // Further paging
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
                        var notification = getString(R.string.noResults)
                        Log.d("TopActivity", notification)
                        val toast = Toast.makeText(
                            this@TopActivity,
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
                Log.e("TopActivity", getString(R.string.jikanFail), exception)
                Toast.makeText(
                    this@TopActivity,
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