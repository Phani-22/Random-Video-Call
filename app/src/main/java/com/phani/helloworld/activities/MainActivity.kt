package com.phani.helloworld.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phani.helloworld.databinding.ActivityMainBinding
import com.phani.helloworld.models.User
import org.jetbrains.annotations.NotNull


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )
    private val requestCode = 9
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        val currentUser = mFirebaseAuth.currentUser

        if (currentUser != null) {
            mDatabase.reference.child("profiles").child(currentUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(@NonNull @NotNull dataSnapshot: DataSnapshot) {
                        user = dataSnapshot.getValue(User::class.java) ?: User()

                        Glide.with(this@MainActivity).load(user.profile)
                            .into(mBinding.userProfilePicture)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        mBinding.btnFind.setOnClickListener {
            if (isPermissionGranted()) {

                val intent = Intent(this, CallConnectActivity::class.java)
                intent.putExtra("Profile", user.profile)
                startActivity(intent)
            }
            askPermission()
        }

        mBinding.btnLogOut.setOnClickListener {
            mFirebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun isPermissionGranted(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}