package com.example.hackverse

import Users
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.hackverse.DataModels.Hackathon
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Account : Fragment() {

    // Firebase instances
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    // Views
    private lateinit var profilePic: ImageView
    private lateinit var userNameText: TextView
    private lateinit var userIDText: TextView
    private lateinit var userEmailInput: TextView
    private lateinit var userContactInput: TextView
    private lateinit var userLinkInput: TextView
    private lateinit var urlUpdate : TextInputEditText
    private lateinit var updateButton : Button
    private lateinit var setNewButton : Button
    private lateinit var layout_url_display : LinearLayout
    private lateinit var layout_update_url : LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        profilePic = view.findViewById(R.id.display_profile_pic)
        userNameText = view.findViewById(R.id.display_userName)
        userIDText = view.findViewById(R.id.display_userID)
        userEmailInput = view.findViewById(R.id.account_email_display)
        userContactInput = view.findViewById(R.id.account_contact_display)
        userLinkInput = view.findViewById(R.id.account_profile_url_display)
        urlUpdate = view.findViewById(R.id.url_field)
        updateButton=view.findViewById(R.id.update_profile_picture)
        setNewButton=view.findViewById(R.id.set_new)
        layout_url_display=view.findViewById(R.id.layout_url)
        layout_update_url=view.findViewById(R.id.layout_updateUrl)


        setNewButton.visibility=View.GONE
        layout_update_url.visibility=View.GONE
        // Get the current user's ID
        val currentUserID = auth.currentUser?.uid

        if (currentUserID != null) {
            loadUserDetails(currentUserID)
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }

        updateButton.setOnClickListener {
            setNewButton.visibility=View.VISIBLE
            updateButton.visibility=View.GONE
            layout_update_url.visibility=View.VISIBLE
            layout_url_display.visibility=View.GONE
            setNewButton.text = "Update Profile URL"

            setNewButton.setOnClickListener {
                updateProfileUrl()
            }

        }
    }

    private fun loadUserDetails(userID: String) {
        // Reference to the user's node in the database
        val userRef = database.child("Users")
        userRef.child(userID).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert the snapshot into a Hackathon object
                val users = snapshot.getValue(Users::class.java)
            if (users != null){

              userNameText.text=users.name
                userIDText.text=users.userID
                userEmailInput.setText(users.userEmail)
                userContactInput.setText(users.userNumber)
                userLinkInput.setText(users.pfpUrl)

                Glide.with(this@Account).load(users.pfpUrl).placeholder(R.drawable.default_picture).into(profilePic)


            }
                  }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching hackathon: ${error.message}")
            }


        })


    }

    private fun updateProfileUrl(){
        val newUrl = urlUpdate.text
        if (newUrl != null) {
            if(newUrl.isNotEmpty()){
                Glide.with(this@Account).load(newUrl.toString()).placeholder(R.drawable.default_picture).into(profilePic)
                showConfirmationDialog()
            } else{
                showDialogForEmptyField()
            }
        }


    }

    private fun showConfirmationDialog(){
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        val userKey = FirebaseAuth.getInstance().currentUser?.uid
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Action")
        builder.setMessage("Do You Want to set this Profile Picture?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            //Yes button
            if (userKey != null) {
                userRef.child(userKey).child("pfpUrl").setValue(urlUpdate.text.toString()).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile Pic Changed Successfully!", Toast.LENGTH_SHORT).show()
                    layout_update_url.visibility=View.GONE
                    layout_url_display.visibility=View.VISIBLE

                    setNewButton.visibility=View.GONE
                    updateButton.visibility=View.VISIBLE
                    loadUserDetails(userKey)
                    updateCommentDetails(urlUpdate.text.toString())
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Failed to set Profile Pic! ", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.dismiss()

        }
        builder.setNegativeButton("No"){ dialog, _ ->
            //No button
            dialog.dismiss()
        }
        val dial = builder.create()
        dial.show()
    }
    private fun showDialogForEmptyField(){
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        val userKey = FirebaseAuth.getInstance().currentUser?.uid
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Action")
        builder.setMessage("Do You Want to proceed with empty Profile Pic?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            //Yes button
            if (userKey != null) {
                userRef.child(userKey).child("pfpUrl").setValue("").addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile Pic Changed Successfully!", Toast.LENGTH_SHORT).show()
                    layout_update_url.visibility=View.GONE
                    layout_url_display.visibility=View.VISIBLE
                    loadUserDetails(userKey)
                    updateCommentDetails(urlUpdate.text.toString())
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Failed to set Profile Pic! ", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.dismiss()

        }
        builder.setNegativeButton("No"){ dialog, _ ->
            //No button
            dialog.dismiss()
        }
        val dial = builder.create()
        dial.show()
    }
private fun updateCommentDetails( newPfpUrl : String){
    val uid=FirebaseAuth.getInstance().currentUser?.uid
    val commentRef = FirebaseDatabase.getInstance().getReference("Comments")

    commentRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (hackathonSnapshot in snapshot.children) {
                if (uid != null) {
                    if (hackathonSnapshot.hasChild(uid)) {
                        val commentsRef = hackathonSnapshot.child(uid)
                        for (commentSnapshot in commentsRef.children) {
                            val commentDetailRef = commentSnapshot.ref
                            commentDetailRef.child("commentUserPfpUrl").setValue(newPfpUrl)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error Fetching User Details!",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }
        }

        override fun onCancelled(error: DatabaseError) {

            Toast.makeText(requireContext(), "Error Fetching Data: ${error.message}", Toast.LENGTH_SHORT).show()
        }


    })
}


}
