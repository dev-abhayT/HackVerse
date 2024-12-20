package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignInActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onStart(){
        super.onStart()
        val currentUser : FirebaseUser? =firebaseAuth.currentUser
        if (currentUser !=null)
        {
            startActivity(Intent(this,HomeActivity::class.java))
            this.finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        firebaseAuth=FirebaseAuth.getInstance()

        val emailEt = findViewById<TextInputEditText>(R.id.emailEt)
        val passEt = findViewById<TextInputEditText>(R.id.passwordEt)
        val signInBtn = findViewById<Button>(R.id.gotohome)
        val gotosignup = findViewById<TextView>(R.id.gotosignup)
        gotosignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        signInBtn.setOnClickListener {
            val email=emailEt?.text.toString().trim()
            val pass = passEt?.text.toString().trim()
            if(email.isNotEmpty() && pass.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        this.finish()
                    } else
                        Toast.makeText(
                            this,
                            "Error Signing In: ${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                }
            else{
                Toast.makeText(this, "No Empty Fields Allowed!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}