package com.udacity.project4.views.activities.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.AuthActBinding
import com.udacity.project4.viewModels.AuthState
import com.udacity.project4.views.activities.reminders.RemindersAct
import com.udacity.project4.viewModels.AuthViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthAct : AppCompatActivity() {

    private var binding: AuthActBinding? = null
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_act)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.auth_act
        )

        //TODO:FOR REVIEWER KINDLY EXTRACT SHA1 FROM TERMINAL AND ADD IT TO YOU FIREBASE
        setAllViewModelObservers()

        itemsClicks()



    }


    private fun setAllViewModelObservers() {


        viewModel.authState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthState.AUTHENTICATED -> {


                    startActivity(Intent(this@AuthAct, RemindersAct::class.java))
                    finish()

                }
                else -> {//USER NEED TO LOG IN
                    binding?.signInBtn?.visibility=View.VISIBLE


                }
            }
        }
    }





    private fun itemsClicks() {

        binding?.signInBtn?.setOnClickListener {
            signInFlow()
        }
    }

    private fun signInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )


        //START ACTIVITY FOR RESULT DEPRECATED SO I WILL GO WITH LAUNCHER:
        val authIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        resultLauncher.launch(authIntent)


    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {


            val data: Intent? = result.data
            Log.d("TAG", "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}
