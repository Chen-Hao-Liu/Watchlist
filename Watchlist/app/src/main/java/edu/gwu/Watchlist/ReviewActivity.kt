package edu.gwu.Watchlist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReviewActivity : AppCompatActivity()  {
    private lateinit var media : String
    private lateinit var showTitle: String
    private lateinit var score : String
    private lateinit var review : String
    private lateinit var mal_id : String
    private lateinit var ratingBar: RatingBar
    private lateinit var writeReview: EditText
    private lateinit var submit: Button
    private lateinit var readOthers: Button
    private lateinit var navBar: BottomNavigationView
    private lateinit var src: Source

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val intent = getIntent()
        src = intent.getParcelableExtra<Source>("source")!!
        media = intent.getStringExtra("media")
        score = src.userScore
        showTitle = src.title
        review = src.userReview
        mal_id = src.mal_id

        title = "My Review $showTitle"

        ratingBar = findViewById(R.id.ratingBar)
        writeReview = findViewById(R.id.writeReview)
        submit = findViewById(R.id.submitReview)
        readOthers = findViewById(R.id.readReviews)
        navBar = findViewById(R.id.navBar_Review)

        // initially disable submit button
        submit.isEnabled = false
        submit.background = getDrawable(R.drawable.rounded_button_unselected)

        // If previous value exists, initialize rating
        if(score != "N/A"){
            ratingBar.rating = score.toFloat()
        }
        // If previous review exists, initialize writeReview
        if(review != ""){
            writeReview.setText(review)
        }

        // Track changes to writeReview textbox
        writeReview.addTextChangedListener(textWatcher)
        // Track changes to rating bar
        ratingBar.setOnRatingBarChangeListener(object : RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                // Make sure review is not empty
                if(writeReview.getText().toString() != ""){
                    submit.isEnabled = true
                    submit.background = getDrawable(R.drawable.rounded_button)
                }
            }
        })

        // Track navbar changes
        navBar.setSelectedItemId(R.id.action_list)
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

        // Submit review
        submit.setOnClickListener{ v: View ->
            updateReviews()
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        // Read other reviews
        readOthers.setOnClickListener{ v: View ->
            val intent = Intent(this, OtherReviewsActivity::class.java)
            intent.putExtra("media", media)
            intent.putExtra("source", src)
            startActivity(intent)
        }
    }

    fun updateReviews(){
        // Acquire Firebase instances
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val instance = FirebaseDatabase.getInstance()

        val myEmail = FirebaseAuth.getInstance().currentUser!!.email!!
        val myScore = ratingBar.rating.toString()
        val myReview = writeReview.getText().toString()

        // Add score and review to src object
        src.userScore = myScore
        src.userReview = myReview

        // Add the rating and review to the source object
        var refA = instance.reference.child("/MyList/$uid/$media/$mal_id")
        refA.setValue(src)
            .addOnSuccessListener {
                Log.d("ReviewActivity", "Updated user review in MyList for $showTitle successfully!")
                Toast.makeText(
                    this@ReviewActivity,
                    "Updated user review in MyList for $showTitle successfully!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {
                Log.d("ReviewActivity", "Failed to update user review in MyList for $showTitle!")
                Toast.makeText(
                    this@ReviewActivity,
                    "Failed to update user review in MyList for $showTitle!",
                    Toast.LENGTH_LONG
                ).show()
            }

        // Add to reviews for the specific anime/manga
        val refB = instance.reference.child("/Reviews/$media/$mal_id/$uid")
        // Update Review Object and set as value for path
        val update = Review(myEmail, myScore, myReview)
        refB.setValue(update)
            .addOnSuccessListener {
                Log.d("ReviewActivity", "Saved review for $showTitle successfully!")
                Toast.makeText(
                    this,
                    "Saved review for $showTitle successfully!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {
                Log.d("ReviewActivity", "Failed to save review for $showTitle!")
                Toast.makeText(
                    this,
                    "Failed to save review for $showTitle!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val passInput: String = writeReview.getText().toString()
            // Review cannot be empty
            val enableButton: Boolean = passInput.isNotEmpty()
            submit.isEnabled = enableButton

            // If button disabled, grey out button
            if(submit.isEnabled) {
                submit.background = getDrawable(R.drawable.rounded_button)
            }else{
                submit.background = getDrawable(R.drawable.rounded_button_unselected)
            }
        }
    }
}