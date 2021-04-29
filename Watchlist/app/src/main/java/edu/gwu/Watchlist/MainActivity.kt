package edu.gwu.Watchlist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class MainActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButt: Button
    private lateinit var registerButt: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var saveSwitch: Switch
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButt = findViewById(R.id.login)
        registerButt = findViewById(R.id.signUp)
        saveSwitch = findViewById(R.id.remember)

        // sharedPreferences for user
        preferences = getSharedPreferences("watchlist", Context.MODE_PRIVATE)

        // Check if saved preferences exist
        if(preferences.contains("save_username") && preferences.contains("save_pass")){
            saveSwitch.isChecked = true
            email.setText(preferences.getString("save_username", ""))
            password.setText(preferences.getString("save_pass", ""))
            // Intialize to enabled
            loginButt.isEnabled = true
            loginButt.background = getDrawable(R.drawable.rounded_button)
        }else{
            saveSwitch.isChecked = false
            // Intialize to disabled
            loginButt.isEnabled = false
            loginButt.background = getDrawable(R.drawable.rounded_button_unselected)
        }

        // track login input
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        loginButt.setOnClickListener{ v: View ->
            submit()
        }
        registerButt.setOnClickListener{ v: View ->
            Log.d("MainActivity", getString(R.string.goReg))
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Track changes to saveSwitch
        saveSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if(!isChecked){
                    // clear existing login credentials
                    preferences.edit().clear().commit()
                }
            }
        })
    }

    private fun submit() {
        val emailInput: String = email.getText().toString().trim()
        val passInput: String = password.getText().toString()
        Log.d("MainActivity", "Email: $emailInput")
        Log.d("MainActivity", "Password: $passInput")

        firebaseAuth
            .signInWithEmailAndPassword(emailInput, passInput)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful){
                    val currentUser = firebaseAuth.currentUser!!
                    val email = currentUser.email
                    val loggedIn = getString(R.string.loggedIn) + " " + email
                    Toast.makeText(this, loggedIn, Toast.LENGTH_LONG).show()

                    // If user decided to save credentials
                    if(saveSwitch.isChecked){
                        // Save login credentials to preferences
                        preferences.edit()
                            .putString("save_username", emailInput)
                            .putString("save_pass", passInput)
                            .commit()
                    }

                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, getString(R.string.invalid), Toast.LENGTH_LONG).show()
                    } else {
                        val failedLogIn = getString(R.string.failedLogIn) + " " + exception
                        Toast.makeText(this, failedLogIn, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Enable button after input detected in search bar
            val emailInput: String = email.getText().toString().trim()
            val passInput: String = password.getText().toString()
            val enableButton: Boolean = emailInput.isNotEmpty() && passInput.isNotEmpty()
            loginButt.isEnabled = enableButton

            // If button disabled, grey out button
            if(loginButt.isEnabled) {
                loginButt.background = getDrawable(R.drawable.rounded_button)
            }else{
                loginButt.background = getDrawable(R.drawable.rounded_button_unselected)
            }
        }
    }
}