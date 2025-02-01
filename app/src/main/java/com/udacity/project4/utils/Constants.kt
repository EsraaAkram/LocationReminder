package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

class Constants {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1
        const val REQUEST_LOCATION_PERMISSION = 2
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 3

        val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
        const val GEOFENCE_RADIUS_IN_METERS = 100f

    }
}