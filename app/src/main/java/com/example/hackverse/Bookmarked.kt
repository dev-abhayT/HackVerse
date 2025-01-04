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

class Bookmarked : Fragment() {
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

        val rootView = inflater.inflate(R.layout.fragment_bookmarked, container, false)


        recyclerView = rootView.findViewById(R.id.recyclerView_bookmarked_hackathons)
        swipeRefresh = rootView.findViewById(R.id.BookmarkedHackathons_swipeRefresh)
        loading = rootView.findViewById(R.id.bookmarked_hackathons_progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(context)


        hackathonAdapter = HackathonAdapter(requireContext(), hackathonList)
        recyclerView.adapter = hackathonAdapter


        loadBookmarkedHackathons()

        swipeRefresh.setOnRefreshListener {
            loadBookmarkedHackathons()
            hackathonAdapter.notifyDataSetChanged()
            swipeRefresh.isRefreshing= false
        }

        return rootView
    }

    private fun loadBookmarkedHackathons() {
        loading.visibility=View.VISIBLE
        recyclerView.visibility=View.GONE
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }


        val hackathonsRef = database.child("Hackathons")

        hackathonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hackathonList.clear()

                for (hackathonSnapshot in snapshot.children) {
                    val hackathon = hackathonSnapshot.getValue(Hackathon::class.java)


                    val registrationsNodeRef = hackathonSnapshot.child("bookmarkers")

                    if (registrationsNodeRef.hasChild(userId)) {
                        hackathon?.let { hackathonList.add(it) }
                    }
                }


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