package com.example.hackverse

import android.app.AlertDialog
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

        val rootView = inflater.inflate(R.layout.fragment_user_creations, container, false)


        recyclerView = rootView.findViewById(R.id.recyclerView_my_creations)
        swipeRefresh = rootView.findViewById(R.id.my_creations_swipeRefresh)
        loading = rootView.findViewById(R.id.my_creations_progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(context)


        hackathonAdapter = HackathonAdapter(requireContext(), hackathonList)
        recyclerView.adapter = hackathonAdapter



        loadCreatedHackathons()
        hackathonAdapter.onItemLongClick={ hackathon ->
            showDeleteDialog(hackathon)
        }
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


        val hackathonsRef = database.child("Hackathons")

        hackathonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hackathonList.clear()

                for (hackathonSnapshot in snapshot.children) {
                    val hackathon = hackathonSnapshot.getValue(Hackathon::class.java)


                    val createNodeRef = hackathonSnapshot.child("creator")

                    if (createNodeRef.hasChild(userId)) {
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

    private fun showDeleteDialog(hackathon: Hackathon){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Delete Hackathon")
        alertDialog.setMessage("Are you sure you want to delete this hackathon?")

        alertDialog.setPositiveButton("Delete") { dialog, _ ->
            delete(hackathon.name)
            dialog.dismiss()
        }

        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialog.create().show()
    }

    private fun delete(hackathonName : String){
        database.child("Hackathons").child(hackathonName).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "${hackathonName} was deleted successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "${hackathonName} couldn't be deleted, please try again!", Toast.LENGTH_SHORT).show()
        }
    }
}