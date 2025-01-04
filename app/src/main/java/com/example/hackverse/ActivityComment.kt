package com.example.hackverse
import Users
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hackverse.Adapters.CommentAdapter
import com.example.hackverse.DataModels.Comments
import com.example.hackverse.DataModels.Hackathon
import com.example.hackverse.databinding.ActivityCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityComment : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var database: DatabaseReference
    private val commentsList = ArrayList<Comments>()
    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        adapter = CommentAdapter(commentsList)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = adapter

        val hackathonName = intent.getStringExtra("Hackathon Name") // Replace with actual hackathon name


        // Load comments from database
        if (hackathonName != null) {
            loadComments(hackathonName)
            loadHackathonDetails(hackathonName)
        }

        binding.commentTextSend.setOnClickListener {
            val commentText = binding.commentTextEnter.text.toString().trim()
            if (commentText.isNotEmpty() && hackathonName!=null) {
                postComment(commentText, hackathonName)
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.commentBackButton.setOnClickListener{
            startActivity(Intent(this, ScreenMainActivity::class.java))
            this.finish()
        }

        binding.swipeRefreshCommentActivity.setOnRefreshListener {
            if (hackathonName != null) {
                reloadAdapter(hackathonName)
            }
            binding.swipeRefreshCommentActivity.isRefreshing = false


        }
    }

    private fun loadComments(hackathonName: String) {
        binding.commentActivityProgressBar.visibility= View.VISIBLE
        binding.commentRecyclerView.visibility=View.GONE
        database.child("Comments").child(hackathonName).get().addOnSuccessListener { snapshot ->
                commentsList.clear()
                snapshot.children.forEach { commentSnapshot ->
                    commentSnapshot.children.forEach {  childSnapshot ->
                        val comment = childSnapshot.getValue(Comments::class.java)
                        comment?.let { commentsList.add(it) }
                    }



                }
                adapter.notifyDataSetChanged()
                binding.commentActivityProgressBar.visibility= View.GONE
                binding.commentRecyclerView.visibility=View.VISIBLE

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
    }

    private fun postComment(commentText: String, hackathonName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        val data = database.child("Users")

        if (uid != null) {
            data.child(uid).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    if(user!=null){
                        val userId = user.userID
                        val userName = user.name
                        val email=user.userEmail
                        val userProfilePicUrl = user.pfpUrl
                        val commentId = database.push().key!!
                        val comment = Comments(
                            commentWriterName = userName,
                            commentMessage = commentText,
                            commentHackathonName = hackathonName,
                            commentUserDatabaseID = userId,
                            commentUserPfpUrl = userProfilePicUrl
                        )

                        // Save under user's node
                        database.child("Comments").child(hackathonName).child(uid).child(commentId).setValue(comment).addOnSuccessListener {
                                commentsList.add(comment)
                                adapter.notifyDataSetChanged()
                                binding.commentTextEnter.text.clear()

                                Toast.makeText(this@ActivityComment, "Comment posted", Toast.LENGTH_SHORT).show()
                                loadComments(hackathonName)
                                updateCommentCount(hackathonName)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ActivityComment, "Failed to post comment", Toast.LENGTH_SHORT).show()
                            }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ActivityComment,"Couldn't fetch User Details: ${error.message}",Toast.LENGTH_SHORT).show()
                }
            })
        }



    }

    private fun loadHackathonDetails(hackathonName: String){
        val data = FirebaseDatabase.getInstance().getReference("Hackathons")
        data.child(hackathonName).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert the snapshot into a Hackathon object
                val hackathon = snapshot.getValue(Hackathon::class.java)
                if(hackathon != null){
                    binding.commentHackathonNameText.text=hackathon.name
                    binding.commentHackathonDate.text="Date: ${hackathon.date}"
                    binding.commentHackathonLoc.text="Location: ${hackathon.location}"
                    binding.commentHackathonUpvoteShow.text="${hackathon.upvotes} UPVOTES"
                    binding.commentHackathonUserName.text="Created By: ${hackathon.createdByUserName}"


                }

    }
            override fun onCancelled(error: DatabaseError) {
                println("Error fetching hackathon: ${error.message}")
            }

    })
}

    private fun reloadAdapter(hackathonName: String){
        loadComments(hackathonName)
        adapter.notifyDataSetChanged()
    }

    private fun updateCommentCount(hackathonName: String){
        val hackRef = FirebaseDatabase.getInstance().getReference("Hackathons")
        hackRef.child(hackathonName).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val hackathon = snapshot.getValue(Hackathon::class.java)
                if(hackathon != null){
                    var commentsCount = hackathon.comments
                    commentsCount += 1
                    hackRef.child(hackathonName).child("comments").setValue(commentsCount)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActivityComment, "Couldn't Update Comment Count! ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
