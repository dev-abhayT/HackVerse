package com.example.hackverse.utils

object Constants {

    // Firebase Database Nodes
    const val USERS = "users"  // Path to users in the database
    const val HACKATHONS = "hackathons"  // Path to hackathons in the database
    const val COMMENTS = "comments"  // Path to comments on hackathons
    const val UPVOTES = "upvotes"  // Path to upvotes count for hackathons
    const val REGISTRATIONS = "registrations"  // Path to registrations count for hackathons

    // Firebase Storage paths
    const val BANNER_IMAGES = "hackathon_banners"  // Path for hackathon banners in Firebase Storage

    // Other constants
    const val HACKATHON_NAME = "name"
    const val HACKATHON_LOCATION = "location"
    const val HACKATHON_DATE = "date"
    const val HACKATHON_BANNER_URL = "bannerUrl"
    const val HACKATHON_DETAILS = "details"
    const val HACKATHON_ID = "hackID"
    const val HACKATHON_UPVOTES = "upvotes"
    const val HACKATHON_COMMENTS = "comments"
    const val HACKATHON_REGISTRATIONS = "registrations"

    // API keys for notifications or any other integrations
    const val BASE_URL = "https://fcm.googleapis.com"
    const val CONTENT_TYPE = "application/json"
    const val SERVER_KEY = "YOUR_SERVER_KEY_HERE"
}
