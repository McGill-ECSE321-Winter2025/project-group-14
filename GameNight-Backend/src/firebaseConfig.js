// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyBJQJBTaBu6GYJaaf3o9OEgMUzntWp7lhU",
  authDomain: "gamenight-1da6e.firebaseapp.com",
  projectId: "gamenight-1da6e",
  storageBucket: "gamenight-1da6e.firebasestorage.app",
  messagingSenderId: "157646701606",
  appId: "1:157646701606:web:efb1ccfc08af819717d2e7"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

export default app;