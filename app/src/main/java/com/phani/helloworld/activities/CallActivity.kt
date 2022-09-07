package com.phani.helloworld.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.phani.helloworld.R
import com.phani.helloworld.databinding.ActivityCallBinding
import com.phani.helloworld.models.InterfaceKt
import com.phani.helloworld.models.User
import java.util.*


class CallActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCallBinding
    private var uniqueId: String = ""
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var friendUserName: String

    private var isPeerConnected = false

    private lateinit var firebaseReference: DatabaseReference

    private var isAudio = true
    private var isVideo = true
    private lateinit var createdBy: String

    private var exitPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mAuth = FirebaseAuth.getInstance()
        firebaseReference = FirebaseDatabase.getInstance().reference.child("users")

        userName = intent.getStringExtra("username").toString()
        val incoming = intent.getStringExtra("incoming").toString()
        createdBy = intent.getStringExtra("createdBy").toString()

        friendUserName = incoming
        if (incoming.contentEquals(friendUserName)) friendUserName = incoming

        setUpWebView()

        mBinding.btnCallMuteUnMute.setOnClickListener {
            isAudio = !isAudio
            javaScriptFunCaller("javascript:toggleAudio(\"$isAudio\")")

            if (isAudio) {
                mBinding.btnCallMuteUnMute.setImageResource(R.drawable.btn_un_mute_normal)
            } else {
                mBinding.btnCallMuteUnMute.setImageResource(R.drawable.btn_mute_normal)
            }
        }
        mBinding.btnVideoOnOff.setOnClickListener {
            isVideo = !isVideo
            javaScriptFunCaller("javascript:toggleVideo(\"$isVideo\")")

            if (isVideo) {
                mBinding.btnVideoOnOff.setImageResource(R.drawable.btn_video_normal)
            } else {
                mBinding.btnVideoOnOff.setImageResource(R.drawable.btn_video_muted)
            }
        }

        mBinding.btnCallEnd.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setUpWebView() {
        mBinding.videoWebView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }
        mBinding.videoWebView.settings.javaScriptEnabled = true
        mBinding.videoWebView.settings.mediaPlaybackRequiresUserGesture = false
        mBinding.videoWebView.addJavascriptInterface(InterfaceKt(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        mBinding.videoWebView.loadUrl(filePath)

        mBinding.videoWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                initializePeer()
            }
        }
    }

    fun initializePeer() {
        uniqueId = generateUniqueId()

        javaScriptFunCaller("javascript:init(\"$uniqueId\")")

        if (createdBy.contentEquals(userName)) {
            if (exitPage)
                return

            firebaseReference.child(userName).child("connId").setValue(uniqueId)
            firebaseReference.child(userName).child("isAvailable").setValue(true)

            mBinding.loadingGrp.visibility = View.GONE
            mBinding.controls.visibility = View.VISIBLE

            FirebaseDatabase.getInstance().reference
                .child("profiles").child(friendUserName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)

                        if (user != null) {
                            Glide.with(this@CallActivity).load(user.profile)
                                .into(mBinding.profileImg)
                            mBinding.profileName.text = user.name
                            mBinding.profileUserCity.text = user.city
                        } else {
                            mBinding.profileImg.setImageResource(R.drawable.demo_user)
                            val text = "Unknown"
                            mBinding.profileName.text = text
                            mBinding.profileUserCity.text = text
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            Handler().postDelayed({
                friendUserName = createdBy

                FirebaseDatabase.getInstance().reference
                    .child("profiles").child(friendUserName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)

                            if (user != null) {
                                Glide.with(this@CallActivity).load(user.profile)
                                    .into(mBinding.profileImg)
                                mBinding.profileName.text = user.name
                                mBinding.profileUserCity.text = user.city
                            } else {
                                mBinding.profileImg.setImageResource(R.drawable.demo_user)
                                val text = "Unknown"
                                mBinding.profileName.text = text
                                mBinding.profileUserCity.text = text
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(friendUserName)
                    .child("connId")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                sendCallConRequest()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }, 2000)
        }
    }

    fun sendCallConRequest() {
        if (!isPeerConnected) {
            Toast.makeText(
                this,
                "You are not connected. Please check your Internet",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        listenToConId()
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    private fun listenToConId() {
        firebaseReference.child(friendUserName).child("connId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) return

                    mBinding.loadingGrp.visibility = View.GONE
                    mBinding.controls.visibility = View.VISIBLE
                    val connId = snapshot.getValue(String::class.java)
                    javaScriptFunCaller("javascript:startCall(\"$connId\")")
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun javaScriptFunCaller(function: String) {
        mBinding.videoWebView.post {
            mBinding.videoWebView.evaluateJavascript(
                function,
                null
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitPage = true
        firebaseReference.child(createdBy).setValue(null)
        finish()
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}