package com.clean.plantsvszombies

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCaptureConfig.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.create_plant_layout.*
import java.io.File
import android.provider.MediaStore
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream


class CreatePlantActivity : AppCompatActivity() {

    private var lastLocation: Location? = null
    private val zombieService: ZombieService = ZombieServiceFactory.provideZombieService()

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_plant_layout)
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1
            )
        }
        uploadButton.visibility = INVISIBLE
        uploadButton.hide()
        uploadButton.setOnClickListener {
            lastLocation?.let {
                createPlant(it.latitude, it.longitude)
            }
        }
        captureButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 1)
            }
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            lat.text = "Latitude: ${it?.latitude}"
            lon.text = "Longitude: ${it?.longitude}"
            lastLocation = it
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras
            val imageBitmap = extras!!.get("data") as Bitmap
            photoView.setImageBitmap(imageBitmap)

            uploadButton.visibility = VISIBLE
            uploadButton.show()

            description.visibility = INVISIBLE
        }
    }

    private fun createPlant(lat: Double, long: Double) {
//        FirebaseApp.initializeApp(this)
//        val storageRef = FirebaseStorage.getInstance()
//        photoView.isDrawingCacheEnabled = true
//        photoView.buildDrawingCache()
//        val bitmap = (photoView.drawable as BitmapDrawable).bitmap
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
//
//        val uploadTask = storageRef.reference.putBytes(data)
//        uploadTask.addOnFailureListener {
//            Log.e("qwer", "image error", it)
//
//        }.addOnSuccessListener {
//            Log.d("qwer", "image ok")
//            Log.d("qwer", it.toString())
//        }
//        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
//            if (!task.isSuccessful) {
//                task.exception?.let {
//                    throw it
//                }
//            }
//            return@Continuation storageRef.reference.downloadUrl
//        }).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val downloadUri = task.result
//                Log.d("qwer", "image ok")
//                Log.d("qwer", downloadUri.toString())
//            } else {
//                Log.e("qwer", "image error")
//            }
//        }

        loading.visibility = VISIBLE
        zombieService.createPlant(Plant(lat = lat, long = long))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    loading.visibility = INVISIBLE
                    finish()
                    Log.d("qwer", "created")
                }, onError = {
                    loading.visibility = INVISIBLE
                    finish()
                    Log.d("qwer", "error creating", it)
                }
            ).addTo(disposables)
    }
}
