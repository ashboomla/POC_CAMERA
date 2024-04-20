package com.example.selfieapp.activities_login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.selfieapp.R
import com.example.selfieapp.activities_login.fragments.PhoneLoginFragment

class PhoneAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_auth)

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, PhoneLoginFragment()).commit()
    }
}
