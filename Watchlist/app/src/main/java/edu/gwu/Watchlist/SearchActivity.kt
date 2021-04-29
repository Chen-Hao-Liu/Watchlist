package edu.gwu.Watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity : AppCompatActivity() {
    private lateinit var searchTitle: EditText
    private lateinit var searchBtn: Button
    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // sharedPreferences for search item
        val preferences = getSharedPreferences("SearchActivity", Context.MODE_PRIVATE)

        // Acquire layout values
        searchTitle = findViewById(R.id.searchTitle)
        searchBtn = findViewById(R.id.searchBtn)
        navBar = findViewById(R.id.navBar)
        val aniManga: Spinner = findViewById(R.id.aniManga)
        val type: Spinner = findViewById(R.id.type)
        val status: Spinner = findViewById(R.id.status)
        val orderBy: Spinner = findViewById(R.id.orderBy)
        val genre: Spinner = findViewById(R.id.genre)
        val rated: Spinner = findViewById(R.id.rated)

        // Initialize searchBtn to false
        searchBtn.isEnabled = false
        searchBtn.background = getDrawable(R.drawable.rounded_button_unselected)

        // Set saved search term if exists
        val preset = preferences.getString("searchTerm", "")
        if(preset != "") {
            searchTitle.setText(preferences.getString("searchTerm", ""))
            searchBtn.isEnabled = true
            searchBtn.background = getDrawable(R.drawable.rounded_button)
        }

        // enable searchBtn upon input to searchTitle
        searchTitle.addTextChangedListener(textWatcher)

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

                // Use different arrays for anime or manga
                if (category == "anime"){
                    spinnerSetup(type, R.array.ATypeArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(status, R.array.AStatusArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(orderBy, R.array.AOrderByArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(genre, R.array.AGenreArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(rated, R.array.ratedArr, android.R.layout.simple_spinner_dropdown_item)
                    rated.isEnabled = true
                }else{
                    spinnerSetup(type, R.array.MTypeArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(status, R.array.MStatusArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(orderBy, R.array.MOrderByArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(genre, R.array.MGenreArr, android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetup(rated, R.array.ratedArr, R.layout.spinner_style)
                    rated.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Another interface callback
            }
        }

        // Search Button
        searchBtn.setOnClickListener { v: View ->
            val input : String = searchTitle.getText().toString()
            val media : String = aniManga.selectedItem.toString()
            val searchType : String = type.selectedItem.toString()
            val searchStatus : String = status.selectedItem.toString()
            val searchOrderBy : String = orderBy.selectedItem.toString()
            val searchGenre : String = genre.selectedItem.toString()
            val searchRated : String = rated.selectedItem.toString()

            val prefEditor = preferences.edit()
            prefEditor.putString("searchTerm", input)
            prefEditor.commit()

            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("searchTerm", input)
            intent.putExtra("media", media)
            intent.putExtra("type", searchType)
            intent.putExtra("status", searchStatus)
            intent.putExtra("orderBy", searchOrderBy)
            intent.putExtra("genre", searchGenre)
            intent.putExtra("rated", searchRated)
            startActivity(intent)
        }

        navBar.setSelectedItemId(R.id.action_find)
        navBar.setOnNavigationItemSelectedListener { item->
            when(item.itemId){
                R.id.action_find ->{

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

    fun spinnerSetup(spin :Spinner, array :Int, style: Int){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            array,
            style
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spin.adapter = adapter
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Enable button after input detected in search bar
            val searchInput: String = searchTitle.getText().toString()
            val enableButton: Boolean = searchInput.isNotEmpty()
            searchBtn.isEnabled = enableButton

            // If button disabled, grey out button
            if(searchBtn.isEnabled) {
                searchBtn.background = getDrawable(R.drawable.rounded_button)
            }else{
                searchBtn.background = getDrawable(R.drawable.rounded_button_unselected)
            }
        }
    }
}