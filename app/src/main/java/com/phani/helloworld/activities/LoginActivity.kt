package com.phani.helloworld.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.phani.helloworld.R
import com.phani.helloworld.databinding.ActivityLoginBinding
import com.phani.helloworld.models.User


class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mFirebaseDatabase: FirebaseDatabase

    companion object {
        private const val RC_SIGN_IN = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser != null) {
            navigateToNextActivity()
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mBinding.btnSignInWithGoogle.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.result
            authWithGoogle(account.idToken)
        }
    }

    private fun authWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val firebaseUser = User(
                        user!!.uid, user.displayName, user.photoUrl.toString(), "Unknown"
                    )
                    mFirebaseDatabase.reference.child("profiles").child(user.uid)
                        .setValue(firebaseUser)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finishAffinity()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    it.exception!!.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    task.exception!!.localizedMessage?.let { Log.e("err", it) }
                }
            }
    }

    private fun navigateToNextActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}