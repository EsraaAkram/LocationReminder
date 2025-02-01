package com.udacity.project4.views.frags

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.SaveReminderFragBinding
import com.udacity.project4.models.ReminderDataItem
import com.udacity.project4.services.GeofenceBroadcastReceiver
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.viewModels.SaveReminderViewModel
import org.koin.android.ext.android.inject

class SaveReminderFrag : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: SaveReminderFragBinding


    private var geofencingClient: GeofencingClient? = null
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)

        PendingIntent.getBroadcast(context,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val runningQOrLater = Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 5
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 6



    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        //checkLocationPermissions()

        if (permissions.isEmpty() ||permissions.containsValue(false)){
            binding.root.let {
                Snackbar.make(
                    it,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            }
        }else{
            checkDeviceLocationSettingsAndStartGeofence()
        }

    }


    private var userReminderData:ReminderDataItem?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.save_reminder_frag,
            container,
            false
        )

        setDisplayHomeAsUpEnabled(true)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())


        binding.selectLocation.setOnClickListener {

            //checkLocationPermissions()

            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragDirections.actionSaveReminderFragmentToSelectLocationFragment())

        }

        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value//
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value//
            val longitude = _viewModel.longitude.value

             userReminderData = ReminderDataItem(
                title = title,
                description = description,
                location = location,
                latitude = latitude,
                longitude = longitude,
            )

            //checkPermissionsToSave(userReminderData)




                if (_viewModel.validateEnteredData(userReminderData!!)){
                    //MAKE SURE USE ALREADY ADDED LOCATION
                    if (
                        userReminderData?.latitude!=null
                        &&
                        userReminderData?.longitude!=null
                    ){
//
                        if (foregroundAndBackgroundLocationPermissionApproved()) {
                            checkDeviceLocationSettingsAndStartGeofence()
                        } else {
                            requestForegroundAndBackgroundLocationPermissions()
                        }



                    }
                }


            


        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        context?.let {
                            ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        })
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        context?.let {
                            ActivityCompat.checkSelfPermission(
                                it, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        }
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d("TAG", "Request foreground only location permission")


//        activity?.let {
//            ActivityCompat.
//            requestPermissions(
//                it,
//                permissionsArray,
//                resultCode
//            )
//        }



        requestPermissions(
            permissionsArray, resultCode
        )
    }



    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("TAG", "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {

            Log.d("TAG", "Deny")
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }


    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->



        if (activityResult.resultCode== Activity.RESULT_OK){
            addGeoForce()
        }else{
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val settings = LocationServices.getSettingsClient(requireActivity())
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsResponse = settings.checkLocationSettings(builder.build())

        locationSettingsResponse.addOnFailureListener {exception ->
            if (exception is ResolvableApiException && resolve) {
                try {

                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution).build()

                    resolutionForResult.launch(intentSenderRequest)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }

        }

        locationSettingsResponse.addOnCompleteListener {
            Log.d("isSuccessful", it.isSuccessful.toString())
            if (it.isSuccessful) {


                addGeoForce()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeoForce() {
        Log.d("userReminderData", userReminderData!!.title.toString())
        if (userReminderData!=null){

            var geo = Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(userReminderData!!.id)

                // Set the circular region of this geofence.
                .setCircularRegion(
                    userReminderData!!.latitude!!,
                    userReminderData!!.longitude!!,
                    Constants.GEOFENCE_RADIUS_IN_METERS
                )

                //.setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                .build()


            //getGeofencingRequest(geo)


            geofencingClient?.addGeofences(getGeofencingRequest(geo),
                geofencePendingIntent)?.run {
                addOnSuccessListener {

                    _viewModel.validateAndSaveReminder(userReminderData!!)
                }
                addOnFailureListener {
                    // Failed to add geofences
                    Toast.makeText(
                        context,
                        "Error !! Can't save the Geofence ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }

    private fun getGeofencingRequest(geo: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geo)
        }.build()
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private fun askForPermissions(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }


    }


    private fun checkLocationPermissions():Boolean {

        return (context?.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
                &&

                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                &&


                ( (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        &&
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ))
    }




    private fun checkPermissionsToSave(userReminderData: ReminderDataItem) {

        if (_viewModel.validateEnteredData(userReminderData)){
            //MAKE SURE USE ALREADY ADDED LOCATION
            if (userReminderData.latitude!=null
                &&
                userReminderData.longitude!=null
            ){

                if (checkLocationPermissions()){

                    checkDeviceLocationSettingsAndStartGeofence()

                }else{
                    binding.root.let {
                        Snackbar.make(
                            it,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {

                            askForPermissions()
                        }.show()
                    }
                }



            }
        }




    }



}
