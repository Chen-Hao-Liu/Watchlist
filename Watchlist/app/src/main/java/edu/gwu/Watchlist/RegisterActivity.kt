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

            Log.d("RegisterActivity", getString(R.string.email) + ": $emailInput")
            Log.d("RegisterActivity", getString(R.string.password) + ": $passInputA")
            Log.d("RegisterActivity", getString(R.string.password) + ": $passInputB")

            // Check that password matches!
            if(passInputA != passInputB){
                Log.d("RegisterActivity", getString(R.string.passmatch))
                val toast = Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.passmatch),
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
                                    val succReg = getString(R.string.succReg) + " " + email
                                    Log.d("RegisterActivity", succReg)
                                    Toast.makeText(
                                        this,
                                        succReg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener {
                                    val failReg = getString(R.string.failReg) + " " + email
                                    Log.d("RegisterActivity", failReg)
                                    Toast.makeText(
                                        this,
                                        failReg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }else{
                            val exception = task.exception
                            if(exception is FirebaseAuthUserCollisionException){
                                Toast.makeText(
                                    this,
                                    "$emailInput: " + getString(R.string.alreadyExist),
                                    Toast.LENGTH_LONG
                                ).show()
                            }else{
                                Toast.makeText(
                                    this,
                                    getString(R.string.regException) + " " + exception,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }

        // Return to login page
        retLogin.setOnClickListener{ v: View ->
            Log.d("RegisterActivity", getString(R.string.retLogin))
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