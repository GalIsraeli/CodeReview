Description
An Android application that lets users capture code snippets with their device camera, extract text via ML Kit OCR, and receive explanations through a Gemini AI–powered chat. Each user can sign in, save chat conversations to Cloud Firestore, and mark chats as favorites—which become publicly viewable by all signed-in users.

Core Features
Authentication (Firebase Auth)
• Email/password sign-in and registration to scope data per user.

Camera & OCR (CameraX + ML Kit)
• Live camera preview and image capture.
• Text recognition to extract code or other text from photos.

Gemini AI Chat
• Conversational interface where captured text is sent to Gemini (“gemini-2.0-flash”) for explanations.
• Real-time input, scrollable message list, and dynamic AI responses.

Cloud Firestore Integration
• Every chat (title, timestamp, messages, favorite flag) is stored under users/{uid}/chats.
• Users can browse their chat history in a dedicated list screen.
• Favorite toggles copy/delete chat data into a top-level favorites collection.
• Any signed-in user can browse and view favorite chats.

View-Only Mode
• Opening an existing chat or favorite displays the full message history and hides the input UI.

Technologies Used
Kotlin (language)

AndroidX CameraX (camera integration)

Firebase ML Kit (text recognition)

Firebase AI SDK (Gemini 2.0 Flash) (AI chat)

Firebase Auth (user sign-in)

Cloud Firestore (chat storage and favorites)

RecyclerView + ViewBinding (UI lists and layouts)

ConstraintLayout (layout structure)

Setup & Running
Clone the repository.

Open in Android Studio (Arctic Fox or newer).

Add google-services.json for your Firebase project under app/.

Enable Email/Password Auth, Firestore, and Firebase AI (Gemini) in your Firebase console.

Publish Firestore rules:

groovy
Copy
Edit
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
Sync Gradle and run on an emulator or device.

Contact / License
This project is provided “as-is” under the MIT License. Contributions and feedback are welcome via GitHub Issues or Pull Requests.
