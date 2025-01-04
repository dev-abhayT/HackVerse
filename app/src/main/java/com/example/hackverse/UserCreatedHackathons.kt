package com.example.hackverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hackverse.DataModels.Hackathon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserCreatedHackathons : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh : SwipeRefreshLayout
    private lateinit var hackathonAdapter: HackathonAdapter
    private lateinit var loading : ProgressBar
    private val hackathonList: MutableList<Hackathon> = mutableListOf()

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout manually
        val rootView = inflater.inflate(R.layout.fragment_user_creations, container, false)

        // Find views manually (no View Binding)
        recyclerView = rootView.findViewById(R.id.recyclerView_my_creations)
        swipeRefresh = rootView.findViewById(R.id.my_creations_swipeRefresh)
        loading = rootView.findViewById(R.id.my_creations_progress_bar)
        // Set up the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up the adapter
        hackathonAdapter = HackathonAdapter(requireContext(), hackathonList)
        recyclerView.adapter = hackathonAdapter

        // Load registered hackathons
        loadCreatedHackathons()

        swipeRefresh.setOnRefreshListener {
            loadCreatedHackathons()
            hackathonAdapter.notifyDataSetChanged()
            swipeRefresh.isRefreshing= false
        }

        return rootView
    }

    private fun loadCreatedHackathons() {
        loading.visibility=View.VISIBLE
        recyclerView.visibility=View.GONE
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the Hackathons node
        val hackathonsRef = database.child("Hackathons")

        hackathonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hackathonList.clear() // Clear the previous data

                for (hackathonSnapshot in snapshot.children) {
                    val hackathon = hackathonSnapshot.getValue(Hackathon::class.java)

                    // Check if the user has registered for this hackathon
                    val createNodeRef = hackathonSnapshot.child("creator")

                    if (createNodeRef.hasChild(userId)) {
                        hackathon?.let { hackathonList.add(it) }
                    }
                }

                // Notify the adapter that the data has changed
                hackathonAdapter.notifyDataSetChanged()
                loading.visibility=View.GONE
                recyclerView.visibility=View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error loading hackathons: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}