package com.beaconscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.*

class MainActivity : BaseBeaconActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener {
            if (!isScanning()) {
                starScan()
            } else {
                stopScan()
            }
        }
    }

    override fun beaconsFound(beaconsList: List<Beacon>) {
        var text = "${beaconsList?.size ?: 0} Beacons\n"
        beaconsList.forEach { beacon ->
            text += "---------------------------------------------\n"
            text += "Type: ${getType(beacon)}\n"
            text += "UUID: ${beacon.id1}\n" // id1 UUDI id2 major id3 menor
            text += "DISTANCE: ${beacon.distance}\n"
        }
        text += "---------------------------------------------\n"
        textView.text = text
    }

    override fun setStateScanning() {
        fab.text = "Stop"
    }

    override fun setStateStopScanning() {
        fab.text = "Start"
    }
}