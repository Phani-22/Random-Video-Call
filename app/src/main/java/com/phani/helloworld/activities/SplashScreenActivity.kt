package com.phani.helloworld.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.phani.helloworld.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySplashScreenBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            navigateToNextActivity()
        }

        mBinding.btnGetStarted.setOnClickListener {
            navigateToNextActivity()
        }
    }

    private fun navigateToNextActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}