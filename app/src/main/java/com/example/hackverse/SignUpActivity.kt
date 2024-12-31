package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        firebaseAuth=FirebaseAuth.getInstance()

        var emailEt = findViewById<TextInputEditText>(R.id.emailEt)
        var passEt = findViewById<TextInputEditText>(R.id.passwordEt)
        var confirmPassEt = findViewById<TextInputEditText>(R.id.confirmPassword)

        var button = findViewById<Button>(R.id.createAccount)

        button.setOnClickListener {
            var email = emailEt?.text.toString().trim()
            var pass = passEt?.text.toString().trim()
            var confirmPass = confirmPassEt?.text.toString().trim()

            if (email != null && pass != null && confirmPass !=null) {
                if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                    if(pass == confirmPass){
                        if (email != null  && pass !=null) {
                            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                                if(it.isSuccessful) {
                                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, ProfileActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    this.finish()
                                } else
                                    Toast.makeText(this, "Account Creation Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()


                            }
                        }
                    } else
                        Toast.makeText(this, "Passwords Do Not Match. Try Again!", Toast.LENGTH_SHORT).show()
                } else
                    Toast.makeText(this, "No Empty Fields Allowed!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}





