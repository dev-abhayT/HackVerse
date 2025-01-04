package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hackverse.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        binding.sendResetLink.setOnClickListener {
            val email = binding.forgotpasswordEmailEt.text
            if (email != null) {
                if(email.isNotEmpty()){
                    firebaseAuth.sendPasswordResetEmail(email.toString().trim()).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            Toast.makeText(this, "Password Reset Link Sent!", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, SignInActivity::class.java))
                            finish()
                        }
                        else
                            Toast.makeText(this, "Email Not Sent: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                    Toast.makeText(this, "Email Field Cannot Remain Empty!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}