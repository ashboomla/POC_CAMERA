package com.example.selfieapp.activities_login.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.selfieapp.R
import kotlinx.android.synthetic.main.fragment_phone_login.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PhoneLoginFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_phone_login, container, false)
        initView(view)
        return view

    }

    private fun initView(view: View) {
        view.button_continue.setOnClickListener {
            var mobile = view.edit_text_mobile.text.toString()
            var phoneVerifyFragment = PhoneVerifyFragment.newInstance(mobile)
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.fragment_container, phoneVerifyFragment).commit()
        }
    }
}