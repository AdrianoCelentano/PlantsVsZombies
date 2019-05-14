package com.clean.plantsvszombies

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.MultiTapKeyListener
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.model.LatLng
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.centerCropTransform
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener {

    private var selectedMarker: Marker? = null
    private val markers: MutableList<Marker> = mutableListOf()
    private var selectedPlant: Plant? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var plants: MutableList<Plant> = mutableListOf()
    private val zombieService: ZombieService = ZombieServiceFactory.provideZombieService()

    private val disposables: CompositeDisposable = CompositeDisposable()

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        healedButton.setOnClickListener {
            plants.remove(selectedPlant)
            selectedPlant = null
            markers.remove(selectedMarker)
            selectedMarker?.remove()
            selectedMarker = null
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior = BottomSheetBehavior.from(plantInfoContainer);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        healButton.setOnClickListener {
            startActivity(Intent(this@MapsActivity, CreatePlantActivity::class.java))
        }
    }

    private fun showBottomsheet(plant: Plant?) {
        selectedPlant = plant
        Picasso.get().setLoggingEnabled(true)
        Picasso.get().load(plant?.href).into(plantImage)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-1.776246, 30.073873), 18.0f))
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMarkerClickListener { marker ->
            selectedMarker = marker
            val plant = plants.find { it.lat == marker.position.latitude }
            showBottomsheet(plant)
            return@setOnMarkerClickListener true
        }
        enableMyLocation()
    }

    private fun loadPlants() {
        zombieService.getPlants()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    plants = it.toMutableList()
                    showPlants()

                }, onError = {
                    Log.d("qwer", "error", it)
                }
            ).addTo(disposables)
    }

    private fun showPlants() {
        plants.forEach {
            if(it.long != 0.0 && it.lat != 0.0) {
                val marker = map.addMarker(MarkerOptions()
                    .position(LatLng(it.lat, it.long))
                    .title("Plant")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.spa_red)))
                markers.add(marker)
            }
        }
    }

    override fun onMyLocationClick(location: Location) {

    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        if (hasNoLocationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else if (::map.isInitialized) {
            map.setMyLocationEnabled(true)
            loadPlants()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (hasNoLocationPermission().not()) {
            map.setMyLocationEnabled(true)
            loadPlants()
        }
    }

    private fun hasNoLocationPermission() =
        (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}
