package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hackverse.DataModels.Hackathon
import com.example.hackverse.databinding.ActivityKnowmoreBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KnowMoreActivity : AppCompatActivity() {
    private lateinit var binding : ActivityKnowmoreBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var data : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowmoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val hackathon_name = intent.getStringExtra("Hackathon Name")
        data = FirebaseDatabase.getInstance().getReference("Hackathons")

        if (hackathon_name != null) {


            data.child(hackathon_name).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Convert the snapshot into a Hackathon object
                    val hackathon = snapshot.getValue(Hackathon::class.java)
                    if(hackathon != null){
                        updateRegisterandUpvoteButton(hackathon_name, binding.knowMoreRegisterButton, binding.knowMoreUpvoteButton)
                        binding.knowMoreTitle.text=hackathon.name
                        binding.knowMoreDate.text="On ${hackathon.date}"
                        binding.knowMoreLoc.text="At ${hackathon.location}"
                        binding.knowMoreUpvotes.text="${hackathon.upvotes} UPVOTES"
                        binding.knowMoreComments.text="${hackathon.comments} COMMENTS"
                        binding.knowMoreUser.text="Created By ${hackathon.createdByUserName}"
                        binding.knowMoreDetails.text=hackathon.details
                        binding.hackIDDisplay.text="HackVerse ID: @${hackathon.hackID}"
                        binding.knowMoreRegistrationsNumber.text="Registrations: ${hackathon.registrations}"
                        binding.knowMoreShareButton.setOnClickListener {
                            val deepLink =
                                "hackverse://hackathon/details/${hackathon.hackathonDatabaseID}"

// Format the content for sharing
                            val shareText = """
    Display your Technical Prowess to the World! 
    Participate in this Hackathon!
    Name: ${hackathon.name}
    Date: ${hackathon.date}
    Location: ${hackathon.location}
    Likes: ${hackathon.upvotes}
    Registrations: ${hackathon.registrations}
    Organized by: ${hackathon.createdByUserName}

    Click here to view the hackathon: 
    $deepLink
""".trimIndent()

// Create the sharing intent
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }

// Show the share sheet (chooser)
                            val chooser = Intent.createChooser(shareIntent, "Share Hackathon via")
                            startActivity(chooser)

                        }
                        binding.knowMoreUpvoteButton.setOnClickListener {
                            updateUpvoteButton(hackathon_name, binding.knowMoreUpvoteButton)
                            binding.knowMoreUpvotes.text="${hackathon.upvotes} UPVOTES"
                        }

                        binding.knowMoreRegisterButton.setOnClickListener {
                            updateRegisterButton(hackathon_name, binding.knowMoreRegisterButton)
                        }

                        binding.backKnowMore.setOnClickListener{
                            startActivity(Intent(this@KnowMoreActivity, ScreenMainActivity::class.java))
                            finish()
                        }
                    }



                }
                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching hackathon: ${error.message}")
                }
            })


        }
        else
            Toast.makeText(this, "Could Not Fetch Hackathon Details!", Toast.LENGTH_SHORT).show()

    }

    private fun updateUpvoteButton(hackathonName: String, upvoteButton: Button){
        firebaseAuth=FirebaseAuth.getInstance()
        val currentuser=firebaseAuth.currentUser
        val userId = currentuser?.uid
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(this, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }

        hackathonRef.child(hackathonName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the current likes count
                    var upvotesCount = snapshot.child("upvotes").getValue(Long::class.java) ?: 0L

                    // Reference to the likes node for this hackathon
                    val upvotesNodeRef = hackathonRef.child(hackathonName).child("upvotesNode")

                    upvotesNodeRef.child(userId).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(likesSnapshot: DataSnapshot) {
                            if (likesSnapshot.exists()) {
                                // User has already liked the hackathon; remove the like
                                upvoteButton.text="Upvote"
                                upvotesNodeRef.child(userId).removeValue()
                                upvotesCount = (upvotesCount - 1).coerceAtLeast(0) // Decrement likes, ensuring it doesn't go below 0
                                hackathonRef.child(hackathonName).child("upvotes").setValue(upvotesCount)
                            } else {
                                // User hasn't liked yet; add the like
                                upvoteButton.text="Upvoted"
                                upvotesNodeRef.child(userId).setValue(true)
                                upvotesCount += 1 // Increment likes
                                hackathonRef.child(hackathonName).child("upvotes").setValue(upvotesCount)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@KnowMoreActivity, "Error updating likes: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@KnowMoreActivity, "Hackathon does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KnowMoreActivity, "Error fetching hackathon: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun updateRegisterButton(hackathonName: String, registerButton: Button){
        firebaseAuth=FirebaseAuth.getInstance()
        val currentuser=firebaseAuth.currentUser
        val userId = currentuser?.uid
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(this, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }

        hackathonRef.child(hackathonName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the current registrations count
                    var registrationsCount = snapshot.child("registrations").getValue(Long::class.java) ?: 0L

                    // Reference to the registrations node for this hackathon
                    val registrationsNodeRef = hackathonRef.child(hackathonName).child("registrationsNode")

                    registrationsNodeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(registrationSnapshot: DataSnapshot) {
                            if (registrationSnapshot.exists()) {
                                // User already registered; unregister them
                                registerButton.text="Register"
                                registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_register,0,0,0)
                                registrationsNodeRef.child(userId).removeValue()
                                registrationsCount = (registrationsCount - 1).coerceAtLeast(0) // Decrement, ensuring it doesn't go below 0
                                hackathonRef.child(hackathonName).child("registrations").setValue(registrationsCount)
                                Toast.makeText(this@KnowMoreActivity, "You have unregistered from $hackathonName", Toast.LENGTH_SHORT).show()
                            } else {
                                // User not registered; register them
                                registerButton.text="Registered"
                                registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_my_hackathons,0,0,0)
                                registrationsNodeRef.child(userId).setValue(true)
                                registrationsCount += 1 // Increment the count
                                hackathonRef.child(hackathonName).child("registrations").setValue(registrationsCount)
                                Toast.makeText(this@KnowMoreActivity, "You have registered for $hackathonName", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@KnowMoreActivity, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@KnowMoreActivity, "Hackathon does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KnowMoreActivity, "Error fetching hackathon: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRegisterandUpvoteButton(hackathonName: String, registerButton: Button, upvoteButton: Button){
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(this, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }

        val registrationsNodeRef = hackathonRef.child(hackathonName).child("registrationsNode")
        val upvotesNodeRef = hackathonRef.child(hackathonName).child("upvotesNode")
        registrationsNodeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(registrationSnapshot: DataSnapshot) {
                if (registrationSnapshot.exists()) {
                    // User already registered; unregister them
                    registerButton.text="Registered"
                    registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_my_hackathons,0,0,0)
                } else {
                    // User not registered; register them
                    registerButton.text="Register"
                    registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_register,0,0,0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KnowMoreActivity, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        upvotesNodeRef.child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(likesSnapshot: DataSnapshot) {
                if (likesSnapshot.exists()) {
                    // User has already liked the hackathon; remove the like
                    upvoteButton.text="Upvoted"
                } else {
                    // User hasn't liked yet; add the like
                    upvoteButton.text="Upvote"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KnowMoreActivity, "Error updating likes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}