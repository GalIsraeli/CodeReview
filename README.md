# Videos
General use - https://drive.google.com/file/d/11Zj8fo3I4w1spvVq6sIdNyo2ClCO20h7/view?usp=drive_link

Riddle solve - https://drive.google.com/file/d/11gigPyY-BdSkAJYwjPwE6dsJaJCnFEHd/view?usp=drive_link


# Code Review App with Gemini AI

An Android application that lets users capture code snippets (or any other text) with their device camera, extract text via ML Kit OCR, and receive explanations through a Google Gemini AI–powered chat. Users sign in with Firebase Auth, save chats in Cloud Firestore, and can mark conversations as favorites (publicly viewable by all signed-in users).

---

## Features

- **User Authentication**  
  • Email/password sign-in and registration (Firebase Auth).  
  • Each user’s data is private under `users/{uid}/chats`.

- **Camera & OCR**  
  • Live camera preview and image capture (CameraX).  
  • Text recognition (Firebase ML Kit OCR) to extract code or other text from photos.

- **Gemini AI Chat**  
  • Conversational interface: captured text is sent to Gemini (via Firebase AI SDK).  
  • Scrollable RecyclerView chat UI, real-time input, dynamic AI responses.  

- **Cloud Firestore Integration**  
  • Save each chat under `users/{uid}/chats/{chatId}` with fields:  
    - `title` (auto-generated), `timestamp`, `isFavorited`, `messages` array.  
  • View past conversations in **ChatsListActivity**.  
  • Mark/unmark “favorite” → copies or deletes that chat under top-level `favorites/{ownerUid}_{chatId}`.  
  • Any signed-in user can browse and view favorite chats in **FavoritesListActivity** / **FavoriteChatActivity**.

- **View-Only Mode**  
  • Opening an existing chat or favorite hides the input UI and displays full history.

---

## Technologies Used

- **Kotlin** (language)  
- **AndroidX CameraX** (camera integration)  
- **Firebase ML Kit** (OCR text recognition)  
- **Firebase AI SDK** (Gemini 2.0 Flash for chat)  
- **Firebase Auth** (user sign-in)  
- **Cloud Firestore** (chat and favorites storage)  
- **RecyclerView + ViewBinding** (UI lists and layouts)  
- **ConstraintLayout** (layout structure)

---

---

## Setup & Installation

1. **Clone the repository** and open it in Android Studio (Arctic Fox or newer).  
2. **Firebase Configuration**  
   - Create a Firebase project.  
   - Enable **Authentication → Email/Password**.  
   - Enable **Cloud Firestore**.  
   - Enable **Firebase AI (Gemini)** in your Firebase console.  
   - Download `google-services.json` and place it under `app/`.  
3. **Firestore Security Rules** (publish these exactly under Firestore → Rules):
   ```groovy
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId}/chats/{chatId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /favorites/{favId} {
         allow read: if request.auth != null;
         allow create: if request.auth != null
                       && request.auth.uid == request.resource.data.ownerUid;
         allow delete: if request.auth != null
                       && request.auth.uid == resource.data.ownerUid;
         allow update: if false;
       }
     }
   }
   ```
4. **Gradle Dependencies are managed via the Firebase BOM:**
  ```groovy
  dependencies {
      implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
  
      // Core libraries
      implementation("androidx.core:core-ktx:1.9.0")
      implementation("androidx.appcompat:appcompat:1.6.0")
      implementation("com.google.android.material:material:1.8.0")
      implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  
      // RecyclerView & ViewBinding
      implementation("androidx.recyclerview:recyclerview:1.3.0")
      buildFeatures {
          viewBinding = true
      }
  
      // CameraX & ML Kit OCR
      implementation("androidx.camera:camera-camera2:1.2.0")
      implementation("androidx.camera:camera-lifecycle:1.2.0")
      implementation("com.google.mlkit:text-recognition:16.0.0")
  
      // Firebase Auth, Firestore, and AI (Gemini)
      implementation("com.google.firebase:firebase-auth-ktx")
      implementation("com.google.firebase:firebase-firestore-ktx")
      implementation("com.google.firebase:firebase-ai-vision:19.0.0")
      implementation("com.google.firebase:firebase-ai-mlkit:16.0.0")
  
      // Coroutines
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
  }
```
5. **Run the App**

  - Sync Gradle.

  - Build and launch on a physical device or emulator.

  - You’ll first see AuthActivity to register/sign in.

  - After signing in, the HomeActivity offers options to start capturing, view history, or view favorites.



