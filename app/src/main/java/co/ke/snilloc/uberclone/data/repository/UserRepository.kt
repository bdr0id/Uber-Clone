package co.ke.snilloc.uberclone.data.repository

import co.ke.snilloc.uberclone.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
}

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserRepository {

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val document = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                val user = if (document.exists()) {
                    document.toObject(User::class.java)
                } else {
                    // Create a default user if document doesn't exist
                    val defaultUser = User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "User",
                        email = currentUser.email ?: "",
                        phoneNumber = currentUser.phoneNumber
                    )
                    createUser(defaultUser)
                    defaultUser
                }
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Please check your account permissions."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Request timeout. Please check your connection."))
                else ->
                    Result.failure(Exception("Failed to get user information: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot update user profile."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Request timeout. Please check your connection."))
                else ->
                    Result.failure(Exception("Failed to update user profile: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun createUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot create user profile."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Request timeout. Please check your connection."))
                else ->
                    Result.failure(Exception("Failed to create user profile: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("User not found."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot delete user profile."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to delete user profile: ${e.message ?: "Unknown error"}"))
            }
        }
    }
}