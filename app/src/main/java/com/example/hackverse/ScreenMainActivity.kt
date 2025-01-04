package com.example.hackverse

import Users
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class ScreenMainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userDataref : DatabaseReference
    private lateinit var navigationView : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        drawerLayout = findViewById(R.id.drawer_layout)

         navigationView = findViewById(R.id.navigation_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        // Set up the ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Load the Home Fragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Home())
                .commit()
        }

        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> {
                    replaceFragment(Home())
                }
                R.id.item_myaccount -> {
                    replaceFragment(Account())
                }
                R.id.item_bookmarked -> {
                    replaceFragment(Bookmarked())
                }
                R.id.item_my -> {
                    replaceFragment(RegisteredHackathons())
                }
                R.id.item_logout -> {
                    handleLogout()
                }

                R.id.item_my_creations -> {
                    replaceFragment(UserCreatedHackathons())
                }
                else -> {
                    replaceFragment(Home())
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userid = currentUser?.uid
        if(userid !=null) {
            userDataref = FirebaseDatabase.getInstance().getReference("Users")
            userDataref.child(userid).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Convert the snapshot into a Hackathon object
                    val users = snapshot.getValue(Users::class.java)
                    if (users != null){

                        updateNavigationHeader(users.name, users.userEmail, users.pfpUrl)




                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching hackathon: ${error.message}")
                }


            })
        }


    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun handleLogout() {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Clear session or preferences if needed
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Ensure the current activity is closed
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateNavigationHeader(name: String, email: String, imageUrl: String){
        val headerView = navigationView.getHeaderView(0) // Access the first header
        val profileImageView = headerView.findViewById<CircleImageView>(R.id.navigation_header_profile_pic)
        val nameTextView = headerView.findViewById<TextView>(R.id.navigation_header_user_name)
        val emailTextView = headerView.findViewById<TextView>(R.id.navigation_header_user_email)

        // Update name and email
        nameTextView.text = "Hi ${name}!"
        emailTextView.text = email

        // Load profile picture using Glide
        Glide.with(this)
            .load(imageUrl) // URL of the image
            .placeholder(R.drawable.default_picture) // Default image if URL is empty or loading
            .error(R.drawable.default_picture) // Fallback image if loading fails
            .into(profileImageView)
    }



}
