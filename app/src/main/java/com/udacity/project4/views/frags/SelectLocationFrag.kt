package com.udacity.project4.views.frags


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.SelectLocationFragBinding
import com.udacity.project4.viewModels.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFrag : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private var binding: SelectLocationFragBinding? = null


    private var mMap: GoogleMap? = null
    private var marker: Marker? = null

    private val REQUEST_LOCATION_PERMISSION = 1

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.select_location_frag, container, false)

        binding?.viewModel = _viewModel
        binding?.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)





        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


        binding?.saveBtn?.setOnClickListener {

            onLocationSelected()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        //checkPermissionAndEnableLocation()
        Log.d("onMapReady", "onMapReady")

        enableMyLocation()

        setMapStyle(map = mMap!!)
        setMapLongClick()
        setPoiClick()

    }

    private fun setMapStyle(map: GoogleMap) {
        try {

            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap?.isMyLocationEnabled = true
        }
        else {
//            activity?.let {
//                ActivityCompat.requestPermissions(
//                    it,
//                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_LOCATION_PERMISSION
//                )
//            }


            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    private fun isPermissionGranted() : Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION)
        } == PackageManager.PERMISSION_GRANTED
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }else{
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation

            }
        }
    }

    private fun setMapLongClick() {
        mMap?.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            mMap!!.clear()
            marker?.remove()

            marker=mMap!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))

                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }


    private fun setPoiClick() {
        mMap?.setOnPoiClickListener { poi ->
            mMap!!.clear()
            marker?.remove()

            marker = mMap!!.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            marker?.showInfoWindow()
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))

        }
    }

    private fun onLocationSelected() {
        if (marker != null) {
            _viewModel.latitude.value = marker!!.position.latitude
            _viewModel.longitude.value = marker!!.position.longitude
            _viewModel.reminderSelectedLocationStr.value = marker!!.title
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.normal_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }










    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.isNotEmpty()
            && permissions.values.first()
        ){

            checkPermissionAndEnableLocation()//RECHECK AGAIN IF TRUE GO ON IF FALSE RE-ASK
        }else{
            //snackBarWithAction()
            _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
        }

    }

    private fun snackBarWithAction() {
        binding?.root?.let {
            Snackbar.make(
                it,
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
        }
    }


    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        checkDeviceLocationSettingsAndStartGeofence(false)
    }



    private fun checkPermissionAndEnableLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        ) {


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            ){
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ){
                    mMap?.isMyLocationEnabled = true
                }else{

                }
            }else{
                mMap?.isMyLocationEnabled = true
            }




        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,

                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            } else {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,

                        )
                )
            }


        }

    }



    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

















    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponseTask = settingsClient
            .checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {

                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution).build()

                    resolutionForResult.launch(intentSenderRequest)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }


            } else {
                binding?.root?.let {
                    Snackbar.make(
                        it,
                        R.string.location_required_error,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {

                        checkDeviceLocationSettingsAndStartGeofence()
                    }.show()
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {



                mMap?.isMyLocationEnabled = true


                val fusedLocationClient = LocationServices
                    .getFusedLocationProviderClient(requireActivity())

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { l: Location? ->

                        Log.d("locationDL", l.toString())

                        if (l != null) {
                            val currentLocation = LatLng(l.latitude, l.longitude)
                            mMap?.addMarker(
                                MarkerOptions().position(currentLocation).title("Marker in My Location")
                            )
                            mMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
                        }


                    }

            }

            Log.d("TAG", it.isSuccessful.toString())
        }
    }


}
