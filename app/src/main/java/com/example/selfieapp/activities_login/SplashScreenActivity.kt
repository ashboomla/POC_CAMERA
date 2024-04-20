package com.example.selfieapp.activities_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.selfieapp.MainActivity
import com.example.selfieapp.R
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {

    var showShimmer: Boolean = true
    private val SPLASH_TIME_OUT: Long =5000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        if (showShimmer){
            shimmer_layout.startShimmer()
        }
        else{
            shimmer_layout.stopShimmer() //stop shimmer
            shimmer_layout.setShimmer(null) //remove shimmer layout

        }

        Handler().postDelayed({
            //executes after timer finsihes
            startActivity(Intent(this,MainActivity::class.java))

            finish()
        },SPLASH_TIME_OUT)
    }
}
