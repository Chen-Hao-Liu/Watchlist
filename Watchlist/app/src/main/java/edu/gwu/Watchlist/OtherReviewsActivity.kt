package edu.gwu.Watchlist

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.math.RoundingMode
import java.text.DecimalFormat

class OtherReviewsActivity : AppCompatActivity() {
    private lateinit var src: Source
    private lateinit var navBar: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: OthersReviewsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var media: String
    private lateinit var reviewName: TextView
    private lateinit var reviewNum: TextView
    private lateinit var reviewMem: TextView
    private lateinit var numReviews: TextView
    private lateinit var reviewImage: ImageView
    private lateinit var reviewScoreTitle: TextView
    private lateinit var reviewScore: TextView
    private lateinit var emptyList: ImageView
    private lateinit var reviewCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others)

        src = intent.getParcelableExtra<Source>("source")!!
        media = intent.getStringExtra("media")

        title = "Audience Reviews for ${src.title}"

        recyclerView = findViewById(R.id.othersRecycler)
        navBar = findViewById(R.id.navBar_Others)
        reviewName = findViewById(R.id.reviewName)
        reviewNum = findViewById(R.id.reviewNum)
        reviewMem = findViewById(R.id.reviewMem)
        numReviews = findViewById(R.id.numReviews)
        reviewImage = findViewById(R.id.reviewImage)
        reviewScoreTitle = findViewById(R.id.reviewScoreTitle)
        reviewScore = findViewById(R.id.reviewScore)
        emptyList = findViewById(R.id.noReviews)
        reviewCard = findViewById(R.id.reviewCardTitle)

        reviewName.setText(src.title)
        val numMem = "${src.members} members"
        reviewMem.setText(numMem)

        // Set different reviewNum depending on anime or manga
        if(media == "anime"){
            val tv = "${src.type} (${src.episodes} eps)"
            reviewNum.setText(tv)
        }else{
            val manga = "${src.type} (Vols: ${src.volumes} Chapters: ${src.chapters})"
            reviewNum.setText(manga)
        }

        // Picasso load image
        if(!src.image_url.isNullOrBlank()){
            Picasso
                .get()
                .setIndicatorsEnabled(true)
            Picasso
                .get()
                .load(src.image_url)
                .into(reviewImage)
        }

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

        // Onclick takes you to myanimelist site
        reviewCard.setOnClickListener { v: View? ->
            val sourceURL = Uri.parse(src.url)
            val intent = Intent(Intent.ACTION_VIEW, sourceURL)
            startActivity(intent)
        }

        // Populate recyclerView
        retrieveSavedReviews(media, src.mal_id)
    }

    fun retrieveSavedReviews(media: String, mal_id: String){
        val reference = FirebaseDatabase.getInstance().reference
            .child("/Reviews/$media/$mal_id")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.d("ListActivity", "Failed to connect to Firebase!", error.toException())
                Toast.makeText(
                    this@OtherReviewsActivity,
                    "Failed to retrieve review from DB!",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                // Calculate average
                var sum = 0.0
                var total = 0
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING

                // keep list of reviews to send over to adapter
                val reviews = mutableListOf<Review>()
                snapshot.children.forEach { child: DataSnapshot ->
                    val rev = child.getValue(Review::class.java)
                    // If not null, add to list
                    if(rev != null){
                        sum += rev.score.toDouble()
                        total++
                        reviews.add(rev)
                    }
                }

                // In case your list is empty
                if(reviews.isEmpty()){
                    // set score title
                    reviewScoreTitle.setText("No Scores")
                    reviewScore.setText("N/A")
                    numReviews.setText("No Reviews")

                    // make empty icon available
                    emptyList.visibility = View.VISIBLE
                    Log.d("OtherReviewsActivity", "No reviews available for ${src.title}!")
                    Toast.makeText(
                        this@OtherReviewsActivity,
                        "No reviews available for ${src.title}!",
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    emptyList.visibility = View.GONE

                    // Calculate overall score
                    var divide : Double = sum/total
                    var average = df.format(divide)

                    // set score title and score accordingly
                    reviewScoreTitle.setText("Overall Score")
                    reviewScore.setText(average.toString())
                    numReviews.setText("$total Reviews")
                    reviewScore
                        .setCompoundDrawablesWithIntrinsicBounds(
                            this@OtherReviewsActivity.getDrawable(android.R.drawable.btn_star_big_on),
                            null,
                            null,
                            null
                        )
                    reviewScore.setTextColor(Color.BLACK)
                    reviewScoreTitle.setTextColor(Color.BLACK)
                }

                // Call adapter
                reviewAdapter = OthersReviewsAdapter(reviews)
                recyclerView.adapter = reviewAdapter
                layoutManager = LinearLayoutManager(this@OtherReviewsActivity)
                recyclerView.layoutManager = layoutManager
            }
        })
    }
}