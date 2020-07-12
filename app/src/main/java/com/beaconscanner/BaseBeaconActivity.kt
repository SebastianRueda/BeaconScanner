package com.beaconscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.os.RemoteException
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.altbeacon.beacon.*

abstract class BaseBeaconActivity : AppCompatActivity(), BeaconConsumer {
    companion object {
        const val LOCATION_REQUEST_CODE = 201
    }

    protected var beaconManager: BeaconManager? = null
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        beaconManager = BeaconManager.getInstanceForApplication(this)
    }

    protected fun starScan() {
        if (!checkBluetooth()) {
            return
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE
            )

            return
        }

        if (beaconManager?.isBound(this) == false) {
            beaconManager!!.bind(this)
        }

        setStateScanning()
    }

    protected fun stopScan() {
        beaconManager?.unbind(this)
        setStateStopScanning()
    }

    override fun onResume() {
        super.onResume()
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24")) //IBEACON
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
    }

    override fun onPause() {
        super.onPause()
        stopScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_REQUEST_CODE -> { starScan() }
        }
    }

    override fun onBeaconServiceConnect() {
        beaconManager?.addRangeNotifier { beacons, _ ->
            beaconsFound(beacons.toList())
        }

        try {
            beaconManager?.startRangingBeaconsInRegion(Region("Test", null, null, null))
        }catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    protected fun getType(beacon: Beacon): String {
        return when (beacon.beaconTypeCode) {
            0X00 -> "Eddystone UID"
            0X10 -> "Eddystone URL"
            0XBEAC -> "Alt Beacon"
            else -> "IBeacon"
        }
    }

    private fun openDialogBluetooth() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bluetooth is OFF")
            .setMessage("You must activate Bluetooth to scan of beacons.")
            .setPositiveButton("Yes") { _, _ ->
                bluetoothAdapter?.enable()
                starScan()
            }
            .setNegativeButton("No") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun checkBluetooth(): Boolean {
        if (bluetoothAdapter == null)
            return false

        if (bluetoothAdapter?.isEnabled == false) {
            openDialogBluetooth()
            return false
        }

        return true
    }

    protected abstract fun beaconsFound(beaconsList: List<Beacon>)
    protected abstract fun setStateScanning()
    protected abstract fun setStateStopScanning()

    protected  fun isScanning(): Boolean = beaconManager?.isBound(this) == true
}