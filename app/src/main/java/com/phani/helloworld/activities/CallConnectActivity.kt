package com.phani.helloworld.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.phani.helloworld.databinding.ActivityCallConnectBinding


class CallConnectActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCallConnectBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private var isObtained = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCallConnectBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        val profile = intent.getStringExtra("Profile")
        Glide.with(this).load(profile).into(mBinding.userImage)

        val username: String = mAuth.uid ?: ""

        mFirebaseDatabase.reference.child("users")
            .orderByChild("status")
            .equalTo(false)
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount > 0) {
                        isObtained = true
                        Log.e("STATUS", "ROOM AVAILABLE")
                        for (childSnap: DataSnapshot in snapshot.children) {
                            mFirebaseDatabase.reference
                                .child("users")
                                .child(childSnap.key ?: "")
                                .child("incoming")
                                .setValue(username)
                            mFirebaseDatabase.reference
                                .child("users")
                                .child(childSnap.key ?: "")
                                .child("status")
                                .setValue(true)
                            val intent = Intent(
                                this@CallConnectActivity,
                                CallActivity::class.java
                            )
                            val incoming =
                                childSnap.child("incoming").getValue(
                                    String::class.java
                                )
                            val createdBy =
                                childSnap.child("createdBy").getValue(
                                    String::class.java
                                )
                            val isAvailable =
                                childSnap.child("isAvailable").getValue(
                                    Boolean::class.java
                                )!!
                            intent.putExtra("username", username)
                            intent.putExtra("incoming", incoming)
                            intent.putExtra("createdBy", createdBy)
                            intent.putExtra("isAvailable", isAvailable)
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        Log.e("CHILDREN COUNT IN ELSE", "NOT >0 FIRST IF MSG")

                        val room: HashMap<String, Any> = HashMap()
                        room["incoming"] = username
                        room["createdBy"] = username
                        room["isAvailable"] = true
                        room["status"] = false

                        mFirebaseDatabase.reference
                            .child("users")
                            .child(username)
                            .setValue(room).addOnSuccessListener {
                                mFirebaseDatabase.reference
                                    .child("users")
                                    .child(username)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(@NotNull snapshot: DataSnapshot) {
                                            if (snapshot.child("status").exists()) {

                                                Log.e("INSIDE ELSE", "JUST GETTING STARTED\n")

                                                if (snapshot.child("status")
                                                        .getValue(Boolean::class.java) == true
                                                ) {
                                                    if (isObtained) return

                                                    isObtained = true

                                                    Log.e(
                                                        "DATA IS MODIFIED",
                                                        "JUST GETTING STARTED\n"
                                                    )

                                                    val intent = Intent(
                                                        this@CallConnectActivity,
                                                        CallActivity::class.java
                                                    )
                                                    val incoming =
                                                        snapshot.child("incoming").getValue(
                                                            String::class.java
                                                        )
                                                    val createdBy =
                                                        snapshot.child("createdBy").getValue(
                                                            String::class.java
                                                        )
                                                    val isAvailable =
                                                        snapshot.child("isAvailable").getValue(
                                                            Boolean::class.java
                                                        )!!
                                                    intent.putExtra("username", username)
                                                    intent.putExtra("incoming", incoming)
                                                    intent.putExtra("createdBy", createdBy)
                                                    intent.putExtra("isAvailable", isAvailable)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}