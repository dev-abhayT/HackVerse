package com.example.hackverse

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hackverse.DataModels.Hackathon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HackathonAdapter(
    private val context: Context,
    private val hackathonList: List<Hackathon>
) : RecyclerView.Adapter<HackathonAdapter.HackathonViewHolder>() {

    var onItemLongClick: ((Hackathon) -> Unit)? = null
    var onShareClick: ((Hackathon) -> Unit)? = null

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    class HackathonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hackathonName: TextView = view.findViewById(R.id.hackathon_name_card)
        val hackathonLocation: TextView = view.findViewById(R.id.hackathon_venue_card)
        val hackathonDate: TextView = view.findViewById(R.id.hackathon_date_card)
        val hackathonBanner: ImageView = view.findViewById(R.id.hackathon_banner_card)
        val bookmark : ImageView=view.findViewById(R.id.bookmark)
        val upvotesDisplay : TextView = view.findViewById(R.id.hackathon_upvote)
        val commentsDisplay : TextView = view.findViewById(R.id.hackathon_comment)
        val upvoteButton: Button = view.findViewById(R.id.hackathon_upvote_btn)
        val registerButton: Button = view.findViewById(R.id.hackathon_register_button)
        val shareButton: Button = view.findViewById(R.id.hackathon_share_btn)
        val knowMoreButton: Button = view.findViewById(R.id.hackathon_know_more_button)
        val commentButton: Button = view.findViewById(R.id.hackathon_comment_button)
        val hackathonCard : CardView = view.findViewById((R.id.hackathon_card))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HackathonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.hackathon_cardview, parent, false)
        return HackathonViewHolder(view)
    }

    override fun onBindViewHolder(holder: HackathonViewHolder, position: Int) {
        val hackathon = hackathonList[position]


        holder.bookmark.visibility=View.VISIBLE
        holder.hackathonName.text = hackathon.name
        holder.hackathonLocation.text = "Location: ${hackathon.location}"
        holder.hackathonDate.text = "Date: ${hackathon.date}"
        holder.upvotesDisplay.text="${hackathon.upvotes} UPVOTES"
        holder.commentsDisplay.text="${hackathon.comments} COMMENTS"
        Glide.with(context).load(hackathon.bannerUrl).error(R.drawable.banner_hackathon).into(holder.hackathonBanner)



        upvoteAndRegisterButtonUpdater(hackathon.name, holder.upvoteButton, holder.registerButton)
        bookmarkButtonUpdator(hackathon.name, holder.bookmark)

        holder.hackathonCard.setOnLongClickListener {

                onItemLongClick?.invoke(hackathon)
            true

        }

        holder.upvoteButton.setOnClickListener {
            upvoteCountUpdate(hackathon, holder.upvoteButton)
        }


        holder.registerButton.text = if (hackathon.registrations > 0) "Registered" else "Register"
        holder.registerButton.setOnClickListener {
            registerClick(hackathon, holder.registerButton)
        }


        holder.shareButton.setOnClickListener {
            val deepLink =
                "hackverse://hackathon/details/${hackathon.hackathonDatabaseID}"


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


            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }


            val chooser = Intent.createChooser(shareIntent, "Share Hackathon via")
            context.startActivity(chooser)


        }

        // Know More Button Functionality
        holder.knowMoreButton.setOnClickListener {
            val intent = Intent(context, KnowMoreActivity::class.java)
            intent.putExtra("Hackathon Name", hackathon.name)
            context.startActivity(intent)
        }

        // Comment Button Functionality
        holder.commentButton.setOnClickListener {
            val intent = Intent(context, ActivityComment::class.java)
            intent.putExtra("Hackathon Name", hackathon.name)
            context.startActivity(intent)
        }

        holder.bookmark.setOnClickListener{
            checkBookmarked(hackathon.name, holder.bookmark)

        }


    }

    override fun getItemCount(): Int = hackathonList.size

    private fun upvoteCountUpdate(hackathon: Hackathon, upvoteButton: Button) {

        val hackathonName = hackathon.name
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(context, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }

        hackathonRef.child(hackathonName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    var upvotesCount = snapshot.child("upvotes").getValue(Long::class.java) ?: 0L


                    val upvotesNodeRef = hackathonRef.child(hackathonName).child("upvotesNode")

                    upvotesNodeRef.child(userId).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(upvotesSnapshot: DataSnapshot) {
                            if (upvotesSnapshot.exists()) {

                                upvoteButton.text="Upvote"
                                upvotesNodeRef.child(userId).removeValue()
                                upvotesCount = (upvotesCount - 1).coerceAtLeast(0) // Decrement likes, ensuring it doesn't go below 0
                                hackathonRef.child(hackathonName).child("upvotes").setValue(upvotesCount)
                            } else {

                                upvoteButton.text="Upvoted"
                                upvotesNodeRef.child(userId).setValue(true)
                                upvotesCount += 1
                                hackathonRef.child(hackathonName).child("upvotes").setValue(upvotesCount)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error updating likes: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Hackathon does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching hackathon: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerClick(hackathon:Hackathon, registerButton: Button) {

        val hackathonName = hackathon.name
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(context, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }

        hackathonRef.child(hackathonName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    var registrationsCount = snapshot.child("registrations").getValue(Long::class.java) ?: 0L


                    val registrationsNodeRef = hackathonRef.child(hackathonName).child("registrationsNode")

                    registrationsNodeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(registrationSnapshot: DataSnapshot) {
                            if (registrationSnapshot.exists()) {
                                // User already registered; unregister them
                                registerButton.text="Register"
                                registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_register,0,0,0)
                                registrationsNodeRef.child(userId).removeValue()
                                registrationsCount = (registrationsCount - 1).coerceAtLeast(0)
                                hackathonRef.child(hackathonName).child("registrations").setValue(registrationsCount)
                                Toast.makeText(context, "You have unregistered from $hackathonName", Toast.LENGTH_SHORT).show()
                            } else {
                                // User not registered; register them
                                registerButton.text="Registered"
                                registerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_my_hackathons,0,0,0)
                                registrationsNodeRef.child(userId).setValue(true)
                                registrationsCount += 1
                                hackathonRef.child(hackathonName).child("registrations").setValue(registrationsCount)
                                Toast.makeText(context, "You have registered for $hackathonName", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Hackathon does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching hackathon: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun upvoteAndRegisterButtonUpdater(hackathonName: String, upvoteButton: Button, registerButton: Button){
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(context, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        upvotesNodeRef.child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(upvotesSnapshot: DataSnapshot) {
                if (upvotesSnapshot.exists()) {

                    upvoteButton.text="Upvoted"
                } else {

                    upvoteButton.text="Upvote"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error updating likes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun addHackathontoBookmarked(hackathonName: String, bookmark: ImageView){
        val currentUser = FirebaseAuth.getInstance().currentUser
        val hackathonData = FirebaseDatabase.getInstance().getReference("Hackathons")
        val uid = currentUser?.uid

        if (uid != null) {
            hackathonData.child(hackathonName).child("bookmarkers").child(uid).setValue(true).addOnSuccessListener {
                Toast.makeText(context, "${hackathonName} was bookmarked", Toast.LENGTH_SHORT).show()
                bookmark.setImageResource(R.drawable.ic_filled_bookmark)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to bookmark: ${hackathonName}: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bookmarkButtonUpdator(hackathonName: String, bookmarkButton: ImageView){
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || hackathonName.isEmpty()) {
            Toast.makeText(context, "User not authenticated or invalid hackathon name", Toast.LENGTH_SHORT).show()
            return
        }
        val bookmarkersNodeRef = hackathonRef.child(hackathonName).child("bookmarkers")

        bookmarkersNodeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookmarkSnapshot: DataSnapshot) {
                if (bookmarkSnapshot.exists()) {
                    bookmarkButton.setImageResource(R.drawable.ic_filled_bookmark)
                } else {

                    bookmarkButton.setImageResource(R.drawable.ic_bookmark)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })



    }
    private fun checkBookmarked(hackathonName: String, bookmark: ImageView){
        val hackathonRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val bookmarkersNodeRef = hackathonRef.child(hackathonName).child("bookmarkers")

        if (userId != null) {
            bookmarkersNodeRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(bookmarkSnapshot: DataSnapshot) {
                    if (bookmarkSnapshot.exists()) {
                        bookmark.setImageResource(R.drawable.ic_bookmark)

                        bookmarkersNodeRef.child(userId).removeValue().addOnCompleteListener {
                            Toast.makeText(context, "${hackathonName} was removed from Bookmarked!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // User not registered; register them
                        bookmark.setImageResource(R.drawable.ic_filled_bookmark)
                        Toast.makeText(context, "${hackathonName} was Bookmarked!", Toast.LENGTH_SHORT).show()
                        addHackathontoBookmarked(hackathonName, bookmark)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error updating registrations: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    }




