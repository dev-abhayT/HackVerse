package com.example.hackverse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hackverse.DataModels.Hackathon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class Home : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var hackathonAdapter: HackathonAdapter
    private lateinit var addBtn: FloatingActionButton
    private val hackathonList = mutableListOf<Hackathon>()
    private lateinit var database: DatabaseReference
    private lateinit var loading : ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        swipeRefreshLayout=view.findViewById(R.id.hackathon_swipe_refresh)
        loading = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.hackathon_recycler_view) // Align ID with your XML file
        addBtn = view.findViewById(R.id.add_button)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        hackathonAdapter = HackathonAdapter(requireContext(), hackathonList)
        recyclerView.adapter = hackathonAdapter


        hackathonAdapter.onShareClick= { hackathon ->
            // Create the deep link to the specific hackathon
            val deepLink = "hackverse://hackathon/details/${hackathon.hackathonDatabaseID}"

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
        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("Hackathons")

        // Load Hackathons
        fetchHackathons()

        addBtn.setOnClickListener {
            startActivity(Intent(activity, CreateHackathon::class.java))
        }
        swipeRefreshLayout.setOnRefreshListener {

            reloadAdapter()

            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun fetchHackathons() {
        recyclerView.visibility=View.GONE
        loading.visibility=View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hackathonList.clear() // Clear the list before adding new data
                for (parentSnapshot in snapshot.children) { // Iterate over the main nodes (e.g., "-OFVbg0N7Z8S1WTmWUBY")

                        val hackathon = parentSnapshot.getValue(Hackathon::class.java)
                        if (hackathon != null) {
                            hackathonList.add(hackathon)
                        }

                }
                hackathonAdapter.notifyDataSetChanged() // Notify the adapter about data changes
                recyclerView.visibility=View.VISIBLE
                loading.visibility=View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                loading.visibility=View.GONE
            }
        })
    }

    private fun reloadAdapter(){
        fetchHackathons()
        hackathonAdapter.notifyDataSetChanged()

    }
}
