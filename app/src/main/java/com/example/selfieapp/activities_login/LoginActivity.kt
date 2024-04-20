package com.example.selfieapp.activities_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.selfieapp.MainActivity
import com.example.selfieapp.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

//FB
    lateinit var callbackManager : CallbackManager

    //Google Login Request Code
    private val RC_SIGN_IN = 7
    //Google Sign In Client
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    //Firebase Auth
    private lateinit var mAuth: FirebaseAuth

    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        callbackManager = CallbackManager.Factory.create()
        buttonFacebookLogin.setReadPermissions(Arrays.asList("email"))

        initLoginWithGoogle()
        init()


    }

    private fun init() {
        button_login_Login.setOnClickListener(this)
        text_view_register_login.setOnClickListener(this)
        sign_in_button_google.setOnClickListener(this)
        button_loginPhone_Login.setOnClickListener(this)
        buttonFacebookLogin.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id)
        {
            R.id.button_login_Login ->{
                login()
            }
            R.id.text_view_register_login ->{
                startActivity(Intent(this, RegisterActivity::class.java))
            }
            R.id.sign_in_button_google ->{
                signIn()
            }
            R.id.button_loginPhone_Login ->{
                startActivity(Intent(this,PhoneAuthActivity::class.java))
            }
            R.id.buttonFacebookLogin -> {
                initFB()
            }
        }
    }

    private fun initLoginWithGoogle() {
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

    }




    private fun initFB() {
        // Initialize Facebook Login button


        buttonFacebookLogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("TAG", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("TAG", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("TAG", "facebook:onError", error)
            }
        })
    }



    //google login
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //google login
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "sign in failed with exception" , Toast.LENGTH_SHORT).show()
            }

        }

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    //google login
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("Login", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "inn", Toast.LENGTH_SHORT).show()
                    val user = mAuth.currentUser

                    var auth = FirebaseAuth.getInstance()
                    auth.currentUser
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Auth Failed", Toast.LENGTH_LONG).show()

                }
            }
    }

    //google login
    fun updateUI(user: FirebaseUser?){
        if(user != null){
            //Do your Stuff
            Toast.makeText(this,"Hello ${user.displayName}",Toast.LENGTH_LONG).show()
        }
    }

    //Regular Sign in
    private fun login() {
        var email = edit_text_email_Login.text.toString()
        var password = edit_text_password_Login.text.toString()
        
       auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this,object: OnCompleteListener<AuthResult>{
                override fun onComplete(task: Task<AuthResult> ) {
                    if (task.isSuccessful) {

                        startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                        Toast.makeText(applicationContext,"login successful", Toast.LENGTH_SHORT).show()
                        auth.currentUser
                    }else{
                        Toast.makeText(applicationContext,"login failed",  Toast.LENGTH_SHORT).show()
                    }

                }
            })
    }

    //FB user token
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                    startActivity(Intent(this@LoginActivity,MainActivity::class.java))


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
    }
}
