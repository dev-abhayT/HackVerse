package com.example.hackverse

import Users
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hackverse.DataModels.Hackathon
import com.example.hackverse.databinding.ActivityCreatehackathonBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateHackathon : AppCompatActivity() {
    private lateinit var binding : ActivityCreatehackathonBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatehackathonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.hackathonDateSelector.setOnClickListener {
            showDateSelector()
        }
        binding.hackathonDate.setOnClickListener {
            showDateSelector()
        }
        binding.hackathonBannerUrl.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val img = s.toString()
                if (img.isNotEmpty()) {
                    Glide.with(this@CreateHackathon).load(img).placeholder(R.drawable.banner_hackathon).error(R.drawable.banner_hackathon)
                        .into(binding.hackathonBannerDisplay)

                } else {
                    binding.hackathonBannerDisplay.setImageResource(R.drawable.banner_hackathon)
                }
            }
        })
        binding.backButton.setOnClickListener{
            startActivity(Intent(this, ScreenMainActivity::class.java))
            finish()
        }
        binding.createHackathon.setOnClickListener {
            val name = binding.hackathonName.text.toString().trim()
            val venue = binding.hackathonVenue.text.toString().trim()
            val date = binding.hackathonDate.text.toString().trim()
            val banner = binding.hackathonBannerUrl.text.toString().trim()
            val details = binding.hackathonDetails.text.toString().trim()
            val hackID = binding.hackathonUniqueId.text.toString().trim()

            if(name.isEmpty()){
                Toast.makeText(this, "Hackathon Name Cannot be Null!", Toast.LENGTH_SHORT).show()
            }

            else if(venue.isEmpty()){
                Toast.makeText(this, "Hackathon Venue Cannot be Null!", Toast.LENGTH_SHORT).show()
            }
            else if(date.isEmpty()){
                Toast.makeText(this, "Hackathon Date Cannot be Null!", Toast.LENGTH_SHORT).show()
            }
            else if(banner.isEmpty() || details.isEmpty()){
                showConfirmationDialog()

            }
            else{
                fetchUserDetailsandSave(name,venue,date,banner,details,hackID)
            }
        }

        binding.hackathonName.addTextChangedListener(createTextWatcher())
        binding.hackathonDate.addTextChangedListener(createTextWatcher())

    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = binding.hackathonName.text.toString().trim()
                val contact = binding.hackathonDate.text.toString().trim()

                if (name.isNotEmpty() && contact.isNotEmpty()) {
                    createHackathonID(name, contact, binding.hackathonUniqueId)
                } else {
                    binding.hackathonUniqueId.setText("")
                }
            }
        }
    }

    private fun showDateSelector() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            dateSetter(null, year, month, day)
        }, year, month, date)

        datePickerDialog.show()
    }

    private fun dateSetter(view: DatePicker?, year: Int, month: Int, day: Int) {
        val selectedDate = Calendar.getInstance().apply {
            set(year, month, day)
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateValue = dateFormat.format(selectedDate.time)

        binding.hackathonDate.setText(dateValue)

    }

    private fun createHackathonID(name: String, date: String, hackID : TextInputEditText){
        val startWord = name.split(" ").joinToString("").uppercase()
        val day = date.take(2)
        val id = "$startWord$day"
        hackID.setText(id)
    }

    private fun fetchUserDetailsandSave(name: String, venue: String, date: String, bannerUrl: String, details : String, hackID: String){
        val currentUser = firebaseAuth.currentUser
        val userData = database.getReference("Users")
        if (currentUser != null) {

            userData.child(currentUser.uid).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {

                    val user = dataSnapshot.getValue(Users::class.java)

                    if (user != null) {

                        saveHackathonDetailsToDatabase(name, venue, date, bannerUrl, details, hackID, user)
                    } else {
                        Toast.makeText(this, "Error: User data not found!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error: User record does not exist!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHackathonDetailsToDatabase(name: String, venue: String, date: String, bannerUrl: String, details : String, hackID: String, creator: Users){
        val currentUser = firebaseAuth.currentUser
        val userData = database.getReference("Users")
        val hackathonData = database.getReference("Hackathons")
        val hackathonDatabaseID = hackathonData.push().key
        val uid = currentUser?.uid
        if(currentUser!=null && hackathonDatabaseID!=null){

            val hackathon = Hackathon(name,venue,date,bannerUrl,details,hackID,0,0,0,creator.name,creator.userID, hackathonDatabaseID?:"")


            hackathonData.child(name).setValue(hackathon).addOnSuccessListener {
                Toast.makeText(this, "Hackathon Created Successfully!", Toast.LENGTH_SHORT).show()
                if (uid != null) {
                    hackathonData.child(name).child("creator").child(uid).setValue(true)
                }
                Intent(this, ScreenMainActivity::class.java)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Error Creating Hackathon: ${it.message}, Please Try Again!", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this, "Null Error! Try Again", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showConfirmationDialog(){
        val name = binding.hackathonName.text.toString().trim()
        val venue = binding.hackathonVenue.text.toString().trim()
        val date = binding.hackathonDate.text.toString().trim()
        val banner = binding.hackathonBannerUrl.text.toString().trim()
        val details = binding.hackathonDetails.text.toString().trim()
        val hackID = binding.hackathonUniqueId.text.toString().trim()
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirm Action")

            builder.setMessage("Do You Want to proceed without entering missing fields?")
            builder.setPositiveButton("Yes") { dialog, _ ->
                //Yes button
                fetchUserDetailsandSave(name,venue,date,banner,details,hackID)
                dialog.dismiss()

            }
            builder.setNegativeButton("No") { dialog, _ ->
                //No button
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()



        }


    }


