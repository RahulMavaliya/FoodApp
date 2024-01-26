package com.example.foodapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.foodapp.databinding.ActivityLoginBinding
import com.example.foodapp.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {
    private var userName: String? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        // initialize of firebase
        auth = Firebase.auth
        database = Firebase.database.reference

        // initialization of google
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

//login with email and password
        binding.loginButton.setOnClickListener {
            // get data from text filed
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
            } else {
                createAUser()
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
            }
        }

        binding.donthaveAccount.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }

        // google sign-in
        binding.googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    // launcher
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Sign-in Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun createAUser() {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUi(user)
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserData()
                        val user = auth.currentUser
                        updateUi(user)
                    } else {
                        Toast.makeText(this, "Sign-in Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveUserData() {
        email = binding.emailAddress.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(userName, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // save data into database
        database.child("user").child(userId).setValue(user)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}