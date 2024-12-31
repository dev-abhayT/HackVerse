package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hackverse.Adapters.HackathonAdapter


import com.example.hackverse.DataModels.Hackathon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var addBtn : FloatingActionButton
    private lateinit var hackRef : DatabaseReference
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var hackathonList: ArrayList<Hackathon>
    private lateinit var adapter: HackathonAdapter
    private lateinit var loading : ProgressBar
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.hackathon_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.hackathon_swipe_refresh)

        recyclerView.layoutManager = LinearLayoutManager(context)
        loading=view.findViewById(R.id.progressBar)
        hackathonList = arrayListOf()
        firebaseAuth=FirebaseAuth.getInstance()
        hackRef=FirebaseDatabase.getInstance().getReference("Hackathons")

        adapter = HackathonAdapter(hackathonList, firebaseAuth, hackRef)
        recyclerView.adapter=adapter

        adapter.onShareClick = { hackathon ->

            // Create the deep link to the specific post
            val deepLink = "hackverse://hackathon/${hackathon.hackID}"

            // Format the content for sharing
            val shareText = """
            Check out this event!
            Event Name: ${hackathon.name}
            Date: ${hackathon.date}
            Venue: ${hackathon.location}
            Upvotes: ${hackathon.upvotes}
            Registrations: ${hackathon.registrations}
            Comments: ${hackathon.comments}
            Organized by: ${hackathon.createdByUserName}
            
            Click here to view the event: 
             $deepLink
            """.trimIndent()

            // Create the sharing intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            // Show the share sheet (chooser)
            val chooser = Intent.createChooser(shareIntent, "Share Event via")
            startActivity(chooser)
        }

        addBtn = view.findViewById(R.id.add_button)
        addBtn.setOnClickListener {

            val intent = Intent(activity, CreateHackathon::class.java)
            startActivity(intent)
        }
        getHackathonList()
        swipeRefreshLayout.setOnRefreshListener {

            reloadAdapter()

            swipeRefreshLayout.isRefreshing = false
        }

        return view




    }

    private fun getHackathonList() {
        recyclerView.visibility = View.GONE
        loading.visibility = View.VISIBLE

        hackRef = FirebaseDatabase.getInstance().getReference("Events")

        hackRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hackathonList.clear()
                if (snapshot.exists()) {
                    for (eveSnap in snapshot.children) {
                        val eveData = eveSnap.getValue(Hackathon::class.java)
                        if (eveData != null) {
                            hackathonList.add(eveData)
                        }
                    }
                    // Notify the adapter that the dataset has changed
                    adapter.notifyDataSetChanged()
                }



                recyclerView.visibility = View.VISIBLE
                loading.visibility = View.GONE


            }

            override fun onCancelled(error: DatabaseError) {
                loading.visibility = View.GONE
            }
        })
    }

    private fun reloadAdapter() {
        getHackathonList()
        // Notify the adapter that the dataset has changed
        adapter.notifyDataSetChanged()
    }
}