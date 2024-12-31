package com.example.hackverse

import Users
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hackverse.databinding.ActivityProfileBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Users")
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up text watchers
        binding.nameField.addTextChangedListener(createTextWatcher())
        binding.contactNumberField.addTextChangedListener(createTextWatcher())

        binding.urlField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                if (url.isNotEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(url)
                        .placeholder(R.drawable.default_picture).into(binding.profilePicture)
                } else {
                    binding.profilePicture.setImageResource(R.drawable.ic_person)
                }
            }
        })

        binding.createProfile.setOnClickListener {
            val userName = binding.nameField.text.toString().trim()
            val contact = binding.contactNumberField.text.toString().trim()
            val imgUrl = binding.urlField.text.toString()
            val uid = binding.userID.text.toString()

            when {
                userName.isEmpty() ->
                    Toast.makeText(this, "Name Field Should Not be Empty!", Toast.LENGTH_SHORT).show()

                contact.isEmpty() ->
                    Toast.makeText(this, "Please Provide a Contact Number", Toast.LENGTH_SHORT).show()

                imgUrl.isEmpty() -> {
                    Toast.makeText(this, "Profile Picture not updated!", Toast.LENGTH_SHORT).show()
                    saveProfile(userName, contact, imgUrl, uid)
                }

                else -> {
                    Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                    saveProfile(userName, contact, imgUrl, uid)
                }
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = binding.nameField.text.toString().trim()
                val contact = binding.contactNumberField.text.toString().trim()

                if (name.isNotEmpty() && contact.isNotEmpty()) {
                    generateUserID(name, contact, binding.userID)
                } else {
                    binding.userID.setText("") // Clear the userID if either field is empty
                }
            }
        }
    }

    private fun generateUserID(name: String, number: String, userID: TextInputEditText) {
        val initials = name.split(" ").joinToString("") { it.take(2).uppercase() }
        val contact = number.take(5)
        val uID = "$initials$contact"
        userID.setText(uID)
    }

    private fun saveProfile(name: String, number: String, imgUrl: String, userID: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "Email Not Available!"
            val userProfile = Users(name, number, imgUrl, userID, email, true)

            database.child(currentUser.uid).setValue(userProfile).addOnSuccessListener {
                Toast.makeText(this, "Profile Created Successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ScreenMainActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error Creating Profile: ${it.message}, Please Try Again!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}