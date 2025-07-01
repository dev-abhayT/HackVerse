# 🚀 HackVerse – A simple Hackathon-Manager App

HackVerse is an Android CRUD(Create, Read, Update, Delete) app built using Kotlin that helps users discover, create, bookmark, and manage hackathons—all from one place.

---

## ✨ Features

### 🛠️ Core Functionality
- **Create, Read, Update, Delete (CRUD)** operations for hackathons.
- **Upvote** hackathons you like.
- **Bookmark** and manage your favorite hackathons.
- **Share** simple hackathon details via other apps.

### 🔐 Authentication
- **Email/Password Sign-up & Sign-in** using Firebase Authentication.
- **Password Recovery** via email in case users forget their credentials.

### 🧭 Navigation
- Switch between:
  - 🔖 Bookmarked Hackathons
  - 🟢 Active Hackathons
  - 📅 Your Created Hackathons
  - ⚙️ Account Page

### 👤 User Personalization
- View and manage **hackathons you created**.
- Delete or update them directly from the app.
- Access your **account page** to see user-specific data.

---

## 🧰 Tech Stack

| Component          | Description                           |
|--------------------|---------------------------------------|
| **Language**        | Kotlin + XML UI                       |
| **Database**         | Firebase Realtime Database (RTDB)     |
| **Authentication**  | Firebase Auth (Email/Password)        |
| **Image Processing**   | Glide for dynamic image support       |

---

## 🔧 Getting Started

Follow these steps to run the project on your local device using **Android Studio**:


### 1. Clone the Repository
- Copy the project URL from the GitHub repository.
```bash
https://github.com/dev-abhayT/HackVerse.git
```

### 2. Get Project from Version Control

- Open Android Studio.
- In the **File** Section, go to **New**->**Project from Version Control**
- Make sure the selected **Version Control** is **Git**
- Paste the copied URL in the given field and click on **Clone**.

### 3. Set Up Firebase
- Go to [Firebase Console](https://console.firebase.google.com/)
- Create a new project
- Enable **Email/Password Authentication**
- Create a **Realtime Database**, set testing rules:
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```
- Download the `google-services.json` file.
- Go to Android Studio and change the Project Structure from the default **Android** to **Project**.
- Simply paste your `google-services.json` file under the `app` directory of your project.

### 4. Sync & Run
- Click **"Sync Project with Gradle Files"**
- Run the project on an installed emulator or Android device

---

## 🧑‍💻 Developed By

**Abhay Tiwari**  
📧 Mail: tiwari.abhay2k6@gmail.com  
💻 [GitHub](https://github.com/dev-abhayT)
🌐 [LinkedIn](https://www.linkedin.com/in/abhay-tiwari-1864762a1/)

---

