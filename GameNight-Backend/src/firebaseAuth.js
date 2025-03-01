import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut } from "firebase/auth";
import app from "./firebaseConfig"; // Import your initialized Firebase app

const auth = getAuth(app);

// Function to register a new user
export const registerUser = async (email, password) => {
	try {
		const userCredential = await createUserWithEmailAndPassword(auth, email, password);
		return userCredential.user; // Successfully registered
	} catch (error) {
		console.error("Error registering user:", error.message);
		throw error; // Handle errors (e.g., email already in use)
	}
};

// Function to log in an existing user
export const loginUser = async (email, password) => {
	try {
		const userCredential = await signInWithEmailAndPassword(auth, email, password);
		return userCredential.user; // Successfully logged in
	} catch (error) {
		console.error("Error logging in:", error.message);
		throw error; // Handle errors (e.g., incorrect password)
	}
};

// Function to log out the user
export const logoutUser = async () => {
	try {
		await signOut(auth);
		console.log("User logged out successfully.");
	} catch (error) {
		console.error("Error logging out:", error.message);
	}
};
