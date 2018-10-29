package com.example.ginggingi.mediaplayer.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat

class PermissionChker{
    private lateinit var mActivity: Activity
    private lateinit var RPListener: RequestPermissionListener
    private var mRequestCode: Int = 0

    fun RequestPermission(mActivity: Activity, permissions: Array<String>,
                          requestCode: Int, RPListener: RequestPermissionListener){

        this.mActivity = mActivity
        this.RPListener = RPListener
        this.mRequestCode = requestCode

        if (!needRequestRuntimePermission()) {
            RPListener.onSuccess()
            return
        }
        requestUnGranted(permissions, requestCode)
    }

    private fun needRequestRuntimePermission() : Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun requestUnGranted(permissions: Array<String>, requestCode: Int) {
        val permissions: Array<String> = FindIsPermissionUngranted(permissions)

        if (permissions.size == 0){
            RPListener.onSuccess()
            return
        }
        return ActivityCompat.requestPermissions(mActivity, permissions, requestCode)
    }

    private fun FindIsPermissionUngranted(permissions: Array<String>) : Array<String> {
        var list: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (!isPermissionGranted(permission))
                list.add(permission)
        }
        return list.toTypedArray()
    }

    private fun isPermissionGranted(permission: String) : Boolean {
        return ActivityCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun RequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResult: IntArray) {
        if (requestCode == mRequestCode) {
            if (grantResult.size > 0) {
                for (gResult in grantResult) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        RPListener.onFailed()
                        return
                    }
                }
            }
            RPListener.onSuccess()
        } else {
            RPListener.onFailed()
        }
    }

    interface RequestPermissionListener {
        fun onSuccess()
        fun onFailed()
    }

}