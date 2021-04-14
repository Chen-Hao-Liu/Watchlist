package edu.gwu.Watchlist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var enterEmail: EditText
    private lateinit var enterPassA: EditText
    private lateinit var enterPassB: EditText
    private lateinit var registerButt: Button
    private lateinit var retLogin: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        enterEmail = findViewById(R.id.emailRegister)
        enterPassA = findViewById(R.id.passRegisterA)
        enterPassB = findViewById(R.id.passRegisterB)
        registerButt = findViewById(R.id.submitReg)
        retLogin = findViewById(R.id.returnLogin)

        // Wait for input
        enterEmail.addTextChangedListener(textWatcher)
        enterPassA.addTextChangedListener(textWatcher)
        enterPassB.addTextChangedListener(textWatcher)

        // Intialize to disabled
        registerButt.isEnabled = false
        registerButt.background = getDrawable(R.drawable.rounded_button_unselected)

        registerButt.setOnClickListener{ v: View ->
            val emailInput: String = enterEmail.getText().toString().trim()
            val passInputA: String = enterPassA.getText().toString()
            val passInputB: String = enterPassB.getText().toString()

            Log.d("RegisterActivity", "Email: $emailInput")
            Log.d("RegisterActivity", "Password: $passInputA")
            Log.d("RegisterActivity", "Password: $passInputB")

            // Check that password matches!
            if(passInputA != passInputB){
                Log.d("RegisterActivity", "Passwords do not match!")
                val toast = Toast.makeText(
                    this@RegisterActivity,
                    "Passwords do not match!",
                    Toast.LENGTH_LONG
                )
                toast.show()
            }else{
                firebaseAuth
                    .createUserWithEmailAndPassword(emailInput, passInputA)
                    .addOnCompleteListener{ task: Task<AuthResult> ->
                        if(task.isSuccessful){
                            val currentUser = firebaseAuth.currentUser!!
                            val email = currentUser.email

                            val uid = FirebaseAuth.getInstance().uid ?: ""
                            val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
                            val user = User(uid, emailInput)

                            ref.setValue(user)
                                .addOnSuccessListener {
                                    Log.d("RegisterActivity", "Successfully registered as $email")
                                    Toast.makeText(
                                        this,
                                        "Successfully registered as $email",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Log.d("RegisterActivity", "Failed to save user: $email")
                                    Toast.makeText(
                                        this,
                                        "Failed to save user: $email",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }else{
                            val exception = task.exception
                            if(exception is FirebaseAuthUserCollisionException){
                                Toast.makeText(
                                    this,
                                    "The account under $emailInput already exists!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }else{
                                Toast.makeText(
                                    this,
                                    "Failed to register: $exception",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }

        // Return to login page
        retLogin.setOnClickListener{ v: View ->
            Log.d("RegisterActivity", "Returning to Login Page")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Enable button after input detected in search bar
            val emailInput: String = enterEmail.getText().toString().trim()
            val passInputA: String = enterPassA.getText().toString()
            val passInputB: String = enterPassB.getText().toString()
            val enableButton: Boolean = emailInput.isNotEmpty() && passInputA.isNotEmpty() && passInputB.isNotEmpty()
            registerButt.isEnabled = enableButton

            // If button disabled, grey out button
            if(registerButt.isEnabled){
                registerButt.background = getDrawable(R.drawable.rounded_button)
            }else{
                registerButt.background = getDrawable(R.drawable.rounded_button_unselected)
            }
        }
    }
}