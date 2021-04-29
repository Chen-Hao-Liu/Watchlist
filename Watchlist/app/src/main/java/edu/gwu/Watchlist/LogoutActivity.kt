package edu.gwu.Watchlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class LogoutActivity : AppCompatActivity() {
    private lateinit var navBar : BottomNavigationView
    private lateinit var myEmail: TextView
    private lateinit var signOut: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        myEmail = findViewById(R.id.signOutEmail)
        signOut = findViewById(R.id.signOut)
        navBar = findViewById(R.id.navBar_logout)
        firebaseAuth = FirebaseAuth.getInstance()

        myEmail.setText(firebaseAuth.currentUser.email)
        signOut.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        navBar.setSelectedItemId(R.id.action_profile)
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
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }
}