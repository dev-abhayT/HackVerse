package com.example.hackverse

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.hackverse.Account
import com.example.hackverse.Home
import com.example.hackverse.R
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle

class ScreenMainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_drawer) // Inflate navigation drawer layout

        // Now access the toolbar from the included layout (activity_main_screen.xml)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up ActionBarDrawerToggle (hamburger icon functionality)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Handle hamburger icon click manually (optional)
        val menuButton = findViewById<ImageView>(R.id.menu_button)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view))) {
                drawerLayout.closeDrawer(findViewById(R.id.nav_view))
            } else {
                drawerLayout.openDrawer(findViewById(R.id.nav_view))
            }
        }

        // Initialize NavigationView and set a listener for menu items
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemClick(menuItem)
            true
        }
    }

    private fun handleNavigationItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.item_home -> {
                // Handle Home button click
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Home()) // Load Home fragment
                    .commit()
            }
            R.id.item_myaccount -> {
                // Handle Account button click
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Account()) // Load Account fragment
                    .commit()
            }
        }
        drawerLayout.closeDrawer(findViewById(R.id.nav_view)) // Close drawer after selection
    }
}
