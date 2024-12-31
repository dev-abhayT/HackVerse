package com.example.hackverse.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.hackverse.R
import com.example.hackverse.DataModels.Hackathon
import com.example.hackverse.utils.Constants
import com.example.hackverse.KnowMoreActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class HackathonAdapter(
    private val hackList: ArrayList<Hackathon>,
    private val auth: FirebaseAuth,
    private val hackRef: DatabaseReference,

    ) : RecyclerView.Adapter<HackathonAdapter.HackathonViewHolder>() {

    var onItemClick: ((Hackathon) -> Unit)? = null
    var onShareClick: ((Hackathon) -> Unit)? = null

    class HackathonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hackName: TextView = itemView.findViewById(R.id.hackathon_name_card)
        val hackLocation: TextView = itemView.findViewById(R.id.hackathon_venue_card)
        val hackDate: TextView = itemView.findViewById(R.id.hackathon_date_card)
        val hackBanner: ImageView = itemView.findViewById(R.id.hackathon_banner_card)
        val hackUpvotes: TextView = itemView.findViewById(R.id.hackathon_upvote)
        val hackComments: TextView = itemView.findViewById(R.id.hackathon_comment)
        val hackBookmark: ImageView = itemView.findViewById(R.id.bookmark)
        val hackRegisterBtn: Button = itemView.findViewById(R.id.hackathon_register_button)
        val hackShareBtn: Button = itemView.findViewById(R.id.hackathon_share_btn)
        val hackUpvoteBtn: Button = itemView.findViewById(R.id.hackathon_comment_btn)
        val hackDetailsBtn: Button = itemView.findViewById(R.id.hackathon_know_more_button)
        val card: CardView = itemView.findViewById(R.id.hackathon_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HackathonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hackathon_cardview, parent, false)
        return HackathonViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hackList.size
    }

    override fun onBindViewHolder(holder: HackathonViewHolder, position: Int) {
        val currentHackathon = hackList[position]

        holder.apply {
            hackName.text = currentHackathon.name
            hackLocation.text = currentHackathon.location
            hackDate.text = currentHackathon.date
            hackUpvotes.text = "${currentHackathon.upvotes} UPVOTES"
            hackComments.text = "${currentHackathon.comments} COMMENTS"

            Glide.with(itemView)
                .load(currentHackathon.bannerUrl)
                .into(hackBanner)

            // Handle upvote button
            updateUpvoteButton(currentHackathon, hackUpvoteBtn)

            // Register button binders
            updateRegisterButton(currentHackathon, hackRegisterBtn)

            // Upvote button click listener
            hackUpvoteBtn.setOnClickListener {
                handleUpvoteButtonClick(currentHackathon)
            }

            // Share button click listener
            hackShareBtn.setOnClickListener {
                onShareClick?.invoke(currentHackathon)
            }

            // Register button click listener
            hackRegisterBtn.setOnClickListener {
                handleRegisterButtonClick(currentHackathon)
            }

            // Hackathon details button
            hackDetailsBtn.setOnClickListener {
                val intent = Intent(itemView.context, KnowMoreActivity::class.java)
                intent.putExtra("hackID", currentHackathon.hackID)
                itemView.context.startActivity(intent)
            }

            // Bookmark click listener
            hackBookmark.setOnClickListener {
                onItemClick?.invoke(currentHackathon)
            }

            // Card click listener
            card.setOnClickListener {
                onItemClick?.invoke(currentHackathon)
            }



        }
    }

    private fun updateUpvoteButton(hackathon: Hackathon, upvoteButton: Button) {
        val hackID = hackathon.hackID
        val myUid = auth.currentUser?.uid

        hackRef.child(Constants.UPVOTES).child(hackID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild(myUid!!)) {
                        upvoteButton.text = "Downvote"
                    } else {
                        upvoteButton.text = "Upvote"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun updateRegisterButton(hackathon: Hackathon, registerButton: Button) {
        val hackID = hackathon.hackID
        val myUid = auth.currentUser?.uid

        hackRef.child(Constants.REGISTRATIONS).child(hackID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild(myUid!!)) {
                        registerButton.text = "Registered"
                    } else {
                        registerButton.text = "Register"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun handleUpvoteButtonClick(hackathon: Hackathon) {
        val hackID = hackathon.hackID
        val myUid = auth.currentUser?.uid
        val upvotesRef = hackRef.child(Constants.UPVOTES).child(hackID)

        upvotesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(myUid!!)) {
                    // Unvote
                    upvotesRef.child(myUid).removeValue()
                    hackRef.child(hackID).child("upvotes").setValue(hackathon.upvotes - 1)
                } else {
                    // Upvote
                    upvotesRef.child(myUid).setValue(true)
                    hackRef.child(hackID).child("upvotes").setValue(hackathon.upvotes + 1)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun handleRegisterButtonClick(hackathon: Hackathon) {
        val hackID = hackathon.hackID
        val myUid = auth.currentUser?.uid
        val registerRef = hackRef.child(Constants.REGISTRATIONS).child(hackID)

        registerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(myUid!!)) {
                    // Unregister
                    registerRef.child(myUid).removeValue()
                    hackRef.child(hackID).child("registrations").setValue(hackathon.registrations - 1)
                } else {
                    // Register
                    registerRef.child(myUid).setValue(true)
                    hackRef.child(hackID).child("registrations").setValue(hackathon.registrations + 1)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
