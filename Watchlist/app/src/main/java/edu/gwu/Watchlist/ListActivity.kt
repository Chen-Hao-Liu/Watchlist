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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListActivity  : AppCompatActivity()  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var listAdapter: ListAdapter
    private lateinit var aniManga: Spinner
    private lateinit var navBar: BottomNavigationView
    private lateinit var emptyList: ImageView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        aniManga = findViewById(R.id.aniMangaB)
        recyclerView = findViewById(R.id.recyclerList)
        navBar = findViewById(R.id.navBar_List)
        emptyList = findViewById(R.id.emptyList)
        emptyList.visibility = View.GONE

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.aniMangaArr,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            aniManga.adapter = adapter
        }

        // Instantiate adapter for retrieving selected item
        aniManga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                val category = parent.getItemAtPosition(pos).toString()
                retrieveSavedSources(category)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Another interface callback
            }
        }

        navBar.setSelectedItemId(R.id.action_list)
        navBar.setOnNavigationItemSelectedListener { item->
            when(item.itemId){
                R.id.action_find ->{
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_list ->{

                }
                R.id.action_top -> {
                    //val intent = Intent(this, MapsActivity::class.java)
                    //startActivity(intent)
                }
                R.id.action_profile ->{
                    //val intent = Intent(this, ProfileActivity::class.java)
                    //startActivity(intent)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    fun retrieveSavedSources(media: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val reference = FirebaseDatabase.getInstance().reference
            .child("/MyList/$uid/$media")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.d("ListActivity", "Failed to connect to Firebase!", error.toException())
                Toast.makeText(
                    this@ListActivity,
                    "Failed to retrieve source from DB!",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val sources = mutableListOf<Source>()
                snapshot.children.forEach { child: DataSnapshot ->
                    val src = child.getValue(Source::class.java)
                    // If not null, add to list
                    if(src != null){
                        sources.add(src)
                    }
                }

                // In case your list is empty
                if(sources.isEmpty()){
                    emptyList.visibility = View.VISIBLE
                    Log.d("ListActivity", "Your $media list is empty!")
                    Toast.makeText(
                        this@ListActivity,
                        "Your $media list is empty!",
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    emptyList.visibility = View.GONE
                }

                listAdapter = ListAdapter(sources, this@ListActivity, media)
                recyclerView.adapter = listAdapter
                layoutManager = LinearLayoutManager(this@ListActivity)
                recyclerView.layoutManager = layoutManager
            }
        })
    }
}